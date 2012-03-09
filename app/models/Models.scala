package models

import anorm._ 
import play.api.db._
import anorm.SqlParser._
import play.api.Play.current

/** 
 * Main model class, immutable as it's a case class 
 **/
case class Shorten( id: Long = -1L, url: String )

/** 
 * Wrapper class to add save and delete method on Shorten case class 
 **/
class EnhancedShorten( shorten: Shorten ) {

    def save() = Shorten.save( shorten )

    def delete() = Shorten.delete( shorten.id )

    def exists() = Shorten.exists( shorten ) 
}

/** 
 * Utility methods for Shorten model 
 **/
object Shorten {

	/**
	 * implicit conversion from Shorten to EnhancedShorten to 
	 * automatically add utilities methods to Shorten case class
	 **/
    implicit def Shorten2EnhancedShorten( shorten: Shorten ) = new EnhancedShorten( shorten )

	/**
	 * Row parser to extract a Shorten object from a database row result
	 **/
    val simple = {
        get[Long]( "shorten.id" ) ~ get[String]( "shorten.url" ) map { 
            case id ~ url => Shorten( id, url ) 
        }
    }

    /**
     * Create a new Shorten in database if not existing or return the existing one
     **/
    def getShortenFor( url:String ) = {
        Shorten.findByUrl( url ).getOrElse {
            Shorten( url = url ).save()
        }
    }

    /**
     * Find a Shorten by its URL value
     **/
    def findByUrl( url:String ) = DB.withConnection { implicit connection =>
        SQL( "select * from shorten s where s.url like {url} " ).on( "url" -> url ).as( Shorten.simple.singleOpt )
    }

    /** 
     * Useless method to try Play 2 parser API
     **/
    def findAllParser() = DB.withConnection { implicit connection =>
        SQL( "select * from shorten order by id asc" ).as( long("id") ~ str("url").map(_.toUpperCase()) map(flatten) *)
    }

    /*---------------- basic model requests ------------------------*/

    /**
     * Return the list of existing Shorten
     **/
    def findAll() = DB.withConnection { implicit connection =>
        SQL( "select * from shorten order by id asc" ).as( Shorten.simple * )
    }

    /**
     * Find a Shorten by its ID
     **/
    def findById( id:Long ) = DB.withConnection { implicit connection =>
        SQL( "select * from shorten s where s.id = {id}" ).on( "id" -> id ).as( Shorten.simple.singleOpt )
    } 

    /**
     * Create a new Shorten on the database
     **/
    def create( shorten: Shorten ) = DB.withConnection { implicit connection =>
        val id: Long = Shorten.nextId()
        SQL( "insert into shorten values ( {id}, {url} )" ).on( "id" -> id, "url" -> shorten.url ).executeUpdate()
        ( id, Shorten( id, shorten.url ) )
    }

    /**
     * Create or update shorten object on the database
     **/
    def save( shorten:Shorten ) = {
        if ( Shorten.findById( shorten.id ).isDefined ) {
            Shorten.update( shorten.id, shorten )
        } else {
            Shorten.create( shorten )._2
        }
    }

    /**
     * Delete a Shorten from database
     **/
    def delete( id: Long ) = DB.withConnection { implicit connection =>
        SQL( "delete from shorten where id = {id}" ).on( "id" -> id ).executeUpdate()
    }

    /**
     * Delete all Shortens from database
     **/
    def deleteAll() = DB.withConnection { implicit connection =>
        SQL( "delete from shorten" ).executeUpdate()
    }

    /**
     * Update a shorten value on the database
     **/
    def update( id: Long, shorten: Shorten ) = DB.withConnection { implicit connection =>
        SQL( "update shorten set url = {url} where id = {id}" ).on( "id"-> id, "url" -> shorten.url ).executeUpdate()
        Shorten( id, shorten.url )
    }

    /**
     * Count existing Shorten from the databse
     **/
    def count() = DB.withConnection { implicit connection => 
        val firstRow = SQL( "select count(*) as s from shorten" ).apply().head 
        firstRow[Long]( "s" )
    }

    /**
     * Get the next unique ID for Shorten
     **/
    def nextId() = DB.withConnection { implicit connection =>
        SQL( "select next value for shorten_seq" ).as( scalar[Long].single )
    }

    /**
     * Check if a particular Shorten exists on the database
     **/
    def exists( id: Long ) = Shorten.findById( id ).isDefined

    /**
     * Check if a particular Shorten exists on the database
     **/
    def exists( shorten: Shorten ) = Shorten.findById( shorten.id ).isDefined
}