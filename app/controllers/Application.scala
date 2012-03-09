package controllers

import play.api._
import play.api.mvc._
import play.api.data.Forms._
import play.api.data._
import models._

/**
 * Main controller for the App
 */
object Application extends Controller {
	/** Get the minified prefix from configuration **/
	val base = Play.current.configuration.getString( "shorten.base" ).getOrElse( "http://fakebase/" )

	val redirect = base
	/** Pattern to validate URL, very dumb **/
	val pattern = "^(https?)://.+$".r

	val start = "http://"
	/** Dataform representing posted values for Shorten creation **/
	val urlForm = Form( "url" -> text )
  
  	/** Index rendering **/
    def index = Action {
        Ok( views.html.index() )
    }

    /** Add a new Shorten on the database **/
    def addShorten = Action { implicit request =>
    	//val maybeUrlValue = urlForm.bindFromRequest.get
    	urlForm.bindFromRequest.fold (
    		formWithErrors => BadRequest( "You need to post a 'url' value!" ),
	    	{ maybeUrlValue =>
	    		val url = Option.apply( maybeUrlValue ) filter { !_.startsWith(start) } toRight ( maybeUrlValue ) fold ( identity, { start + _ } )
	    		url match {
	    			case pattern(v) => Ok( redirect + Shorten.getShortenFor ( url ).id )
	    			case _ => Ok( "wrong" )
	    		}
	    	}
    	)
	}
	
	/** Render all existing Shortens **/
	def all = Action {
		Ok( views.html.all( Shorten.findAll(), redirect ) )
	}
	
	/** Redirect a request from minified URL to actual URL **/
	def redirectTo( id: Long ) = Action {
		Redirect( Shorten.findById ( id ) map { _.url } getOrElse ( base ) );
	}

	/** Render about view **/
	def about = Action {
		Ok( views.html.about() )
	}
 
 	/** Delete a particular Shorten **/
	def delete( id: Long ) = Action {
		Shorten.delete( id )
		Redirect( routes.Application.all() )
	}

 	/** Delete all Shortens **/
	def deleteAll() = Action {
		Shorten.deleteAll()
		Redirect( routes.Application.all() )
	}
}