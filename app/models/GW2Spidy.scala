package models

import play.api.libs.ws._
import play.api.libs.json._
import play.api.cache.Cache
import java.util.concurrent.TimeUnit
import models.Rarity._

/**
 * @author exthulja
 */
object GW2Spidy {

  // {"results":[{"id":0,"name":"Armor","subtypes":[{"id":0,"name":"Coat"},{"id":1,"name":"Leggings"},{"id":2,"name":"Gloves"},{"id":3,"name":"Helm"},{"id":4,"name":"Aquatic Helm"},{"id":5,"name":"Boots"},{"id":6,"name":"Shoulders"}]},{"id":2,"name":"Bag","subtypes":[]},{"id":3,"name":"Consumable","subtypes":[{"id":1,"name":"Food"},{"id":2,"name":"Generic"},{"id":5,"name":"Transmutation"},{"id":6,"name":"Unlock"}]},{"id":4,"name":"Container","subtypes":[{"id":0,"name":"Default"},{"id":1,"name":"Gift Box"}]},{"id":5,"name":"Crafting Material","subtypes":[]},{"id":6,"name":"Gathering","subtypes":[{"id":0,"name":"Foraging"},{"id":1,"name":"Logging"},{"id":2,"name":"Mining"}]},{"id":7,"name":"Gizmo","subtypes":[{"id":0,"name":"Default"},{"id":2,"name":"Salvage"}]},{"id":11,"name":"Mini","subtypes":[]},{"id":13,"name":"Tool","subtypes":[{"id":0,"name":"[[Crafting]]"},{"id":2,"name":"Salvage"}]},{"id":15,"name":"Trinket","subtypes":[{"id":0,"name":"Accessory"},{"id":1,"name":"Amulet"},{"id":2,"name":"Ring"}]},{"id":16,"name":"Trophy","subtypes":[]},{"id":17,"name":"Upgrade Component","subtypes":[{"id":0,"name":"Weapon"},{"id":2,"name":"Armor"}]},{"id":18,"name":"Weapon","subtypes":[{"id":0,"name":"Sword"},{"id":1,"name":"Hammer"},{"id":2,"name":"Longbow"},{"id":3,"name":"Short Bow"},{"id":4,"name":"Axe"},{"id":5,"name":"Dagger"},{"id":6,"name":"Greatsword"},{"id":7,"name":"Mace"},{"id":8,"name":"Pistol"},{"id":10,"name":"Rifle"},{"id":11,"name":"Scepter"},{"id":12,"name":"Staff"},{"id":13,"name":"Focus"},{"id":14,"name":"Torch"},{"id":15,"name":"Warhorn"},{"id":16,"name":"Shield"},{"id":19,"name":"Spear"},{"id":20,"name":"Harpoon Gun"},{"id":21,"name":"Trident"},{"id":22,"name":"Toy"}]}]}

  def loadMainTypes : List[ItemType] = {
    val response = WS.url("http://www.gw2spidy.com/api/v0.9/json/types").get()
    response.map( {r:Response =>
      val results:List[JsValue] = (r.json \ "results").as[List[JsObject]]
      results.map({a:JsValue =>
        val jsSubtypes:List[JsValue] = (a \ "subtypes").as[List[JsObject]]
        val id = (a \ "id").as[Int]
        val name = (a \ "name").as[String]
        val subtypes = jsSubtypes.map({b:JsValue =>
          val id = (b \ "id").as[Int]
          val name = (b \ "name").as[String]
          SubType(id, name)
        })
        ItemType(id, name, subtypes)
      })
    }).await(1000, TimeUnit.MILLISECONDS).get
  }

  // {"count":63,"results":[{"data_id":8939,"name":"Ogre Bag","rarity":1,"restriction_level":0,"img":"https:\/\/dfach8bufmqqv.cloudfront.net\/gw2\/img\/content\/41caa7ec.png",
  // "type_id":2,"sub_type_id":0,"price_last_changed":"2013-01-10 15:11:41 UTC","max_offer_unit_price":646,"min_sale_unit_price":1345,"offer_availability":1766,"sale_availability":452,"gw2db_external_id":23,"sale_price_change_last_hour":0,"offer_price_change_last_hour":0},{"data_id":8940,"name":"Wrangler's Bag","rarity":1,"restriction_level":0,"img":"https:\/\/dfach8bufmqqv.cloudfront.net\/gw2\/img\/content\/41caa7ec.png","type_id":2,"sub_type_id":0,"price_last_changed":"2013-01-10 15:18:04 UTC","max_offer_unit_price":331,"min_sale_unit_price":947,"offer_availability":227,"sale_availability":150,"gw2db_external_id":3756,"sale_price_change_last_hour":0,"offer_price_change_last_hour":0},{"data_id":8941,"name":"Leather Bag","rarity":1,"restriction_level":0,"img":"https:\/\/dfach8bufmqqv.cloudfront.net\/gw2\/img\/content\/ab59e4ae.png","type_id":2,"sub_type_id":0,"price_last_changed":"2013-01-09 23:36:32 UTC","max_offer_unit_price":2030,"min_sale_unit_price":12497,"offer_availability":99,"sale_availability":6,"gw2db_external_id":24311,"sale_price_change_last_hour":0,"offer_price_change_last_hour":0},

  def findItemType(id:Int) : ItemType = {
    mainTypes.find(a => a.id == id).getOrElse(ItemType(-1, "Unknown", List()))
  }

  def findSubType(itemType:ItemType, id:Int) : SubType = {
    itemType.subtypes.find(a => a.id == id).getOrElse(SubType(-1, "Unknown"))
  }

  def loadTradeItems(itemType:ItemType) : List[TradeItem] = {
    val response = WS.url("http://www.gw2spidy.com/api/v0.9/json/all-items/" + itemType.id).get
    response.map({r:Response =>
      val results:List[JsValue] = (r.json \ "results").as[List[JsObject]]
      results.map({a:JsValue =>
        val id = (a \ "data_id").as[Int]
        val name = (a \ "name").as[String]
        val rarity:Rarity = Rarity.fromInt((a \ "rarity").as[Int])
        val itemType:ItemType = findItemType((a \ "type_id").as[Int])
        val subType:SubType = findSubType(itemType, (a \ "sub_type_id").as[Int])
        val level = (a \ "restriction_level").as[Int]
        val maxOffer = (a \ "max_offer_unit_price").as[Int]
        val minSale = (a \ "min_sale_unit_price").as[Int]
        val offerCount = (a \ "offer_availability").as[Int]
        val saleCount = (a \ "sale_availability").as[Int]
        TradeItem(id, name, itemType, rarity, subType, level, maxOffer, minSale, offerCount, saleCount)
      })
    }).await(30000, TimeUnit.MILLISECONDS).get

  }

  import play.api.Play.current

  def mainTypes:List[ItemType] = {
    Cache.getOrElse[List[ItemType]]("GW2Spidy.mainTypes", 30*60)({
      loadMainTypes
    })
  }

  def tradeItems:List[TradeItem] = {
    Cache.getOrElse[List[TradeItem]]("GW2Spidy.tradeItems", 10*60)({
      val types = mainTypes
      val result: List[List[TradeItem]] = for {
        itemType <- types
      } yield {
        val list:List[TradeItem] = loadTradeItems(itemType)
        list
      }
      result.flatten
    })
  }

}
