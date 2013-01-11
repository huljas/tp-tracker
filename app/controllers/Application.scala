package controllers

import play.api._
import play.api.mvc._
import models.{TradingPost, GW2Spidy}

object Application extends Controller {
  
  def index = Action {
    val mainTypes = GW2Spidy.mainTypes
    val tradeItems = GW2Spidy.tradeItems
    val weapons = TradingPost.findWeapons
    Ok(views.html.index(mainTypes, tradeItems, weapons))
  }
  
}