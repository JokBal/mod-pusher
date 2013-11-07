package org.jokbal.pusher.http

import org.vertx.scala.core.Vertx
import org.vertx.scala.core.json.JsonObject
import org.vertx.scala.core.http.{RouteMatcher, HttpServerRequest, HttpServer}
import org.vertx.scala.core.buffer.Buffer

class HttpServerManager(vertx : Vertx,config : JsonObject){

  var server : HttpServer = null
  val SERVER_ENABLED = config.getBoolean("server_enabled",true)
  val SERVER_PORT = config.getNumber("server_port",9999).intValue()

  def startServer() {

    if(SERVER_ENABLED){
      System.out.println("start Http Server!")
      server = vertx.createHttpServer()
      server.requestHandler(this.makeRouteMatcher).listen(SERVER_PORT)
    }

    else throw new Exception("Http Server Config is not defined as true")

  }

  def makeRouteMatcher = {
    System.out.println("Make Routing Matcher")
    var routeMatch = new RouteMatcher()

    routeMatch.post("/apps/:appsId/events",{
      req : HttpServerRequest =>
        req.dataHandler{
          bf : Buffer =>
            val length = bf.length()
            val body = bf.getString(0,length)
            val ev = new EventTrigger(body)
            ev.publishEvent

            req.response().setStatusCode(ev.statusCode)
              .setStatusMessage(ev.statusMessage).end()
        }



    })

    routeMatch.get("/apps/:appsId/channels",{
      req : HttpServerRequest =>
        val trigger = new ChannelTrigger with GetChannels
        trigger.get(req)
        val code = trigger.getCode
        val message = trigger.getMessage
        req.response().setStatusCode(code).setStatusMessage(message).end()

    })

    routeMatch.get("/apps/:appsId/channels/:channelName",{
      req : HttpServerRequest =>
        val trigger = new ChannelTrigger with GetChannel
        trigger.get(req)
        val code = trigger.getCode
        val message = trigger.getMessage
        req.response().setStatusCode(code).setStatusMessage(message).end()
    })

    routeMatch.get("/apps/:appsId/channels/:channelName/users",{
      req : HttpServerRequest =>
        val trigger = new ChannelTrigger with GetUsers
        trigger.get(req)
        val code = trigger.getCode
        val message = trigger.getMessage


        req.response().setStatusCode(code).setStatusMessage(message).end()
    })

    routeMatch



  }

}
