package models

/**
 * @author exthulja
 */
object Rarity extends Enumeration {
  type Rarity = Value
  val Common, Fine, Masterwork, Rare, Exotic, Legendary, WTF, WTF2 = Value

  def fromInt(key:Int) : Rarity.Value = {
    this.apply(key - 1)
  }
}

case class ItemType(id:Int, name:String, subtypes:Seq[SubType]) {
  override def toString: String = name
}

case class SubType(id:Int, name:String) {
  override def toString : String = name
}

import Rarity._

case class TradeItem(id:Int, name:String, itemType:ItemType, rarity:Rarity, subType:SubType, level:Int, maxOffer:Int, minSale:Int, offerCount:Int, saleCount:Int) {
  val flipGain = (minSale * 0.85f - maxOffer) / maxOffer

  override def toString: String = name + " [" + rarity + "] ," + itemType + ", " + subType + " (lvl:" + level + ", offer:" + maxOffer + ", sale:" + minSale + ", flipGain:" + flipGain
}

//             if (item.rarity >= 4 && item.max_offer < 20000 && item.min_sale > 2000 && item.sale_count > 20 && item.offer_count > 10 && item.type_id == 18 && (item.sub_type_id == 0 || item.sub_type_id == 5 || item.sub_type_id == 6 || item.sub_type_id == 12 || item.sub_type_id == 8)) {


object TradingPost {
  def findWeapons:List[TradeItem] = {
    GW2Spidy.tradeItems.filter(p => p.maxOffer < 50000 && p.minSale > 2000 && p.saleCount > 10 && p.itemType.name == "Weapon").sortBy(p => -p.flipGain)
  }
}

