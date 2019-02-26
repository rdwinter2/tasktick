package io.surfkit.lagomhelm.impl

import io.surfkit.lagomhelm.api
import io.surfkit.lagomhelm.api.{LagomhelmService}
import com.lightbend.lagom.scaladsl.api.ServiceCall
import com.lightbend.lagom.scaladsl.api.broker.Topic
import com.lightbend.lagom.scaladsl.broker.TopicProducer
import com.lightbend.lagom.scaladsl.persistence.{EventStreamElement, PersistentEntityRegistry}

/**
  * Implementation of the LagomhelmService.
  */
class LagomhelmServiceImpl(persistentEntityRegistry: PersistentEntityRegistry) extends LagomhelmService {

  override def hello(id: String) = ServiceCall { _ =>
    // Look up the LagomHelm entity for the given ID.
    val ref = persistentEntityRegistry.refFor[LagomhelmEntity](id)

    // Ask the entity the Hello command.
    ref.ask(Hello(id))
  }

  override def useGreeting(id: String) = ServiceCall { request =>
    // Look up the LagomHelm entity for the given ID.
    val ref = persistentEntityRegistry.refFor[LagomhelmEntity](id)

    // Tell the entity to use the greeting message specified.
    ref.ask(UseGreetingMessage(request.message))
  }


  override def greetingsTopic(): Topic[api.GreetingMessageChanged] =
    TopicProducer.singleStreamWithOffset {
      fromOffset =>
        persistentEntityRegistry.eventStream(LagomhelmEvent.Tag, fromOffset)
          .map(ev => (convertEvent(ev), ev.offset))
    }

  private def convertEvent(helloEvent: EventStreamElement[LagomhelmEvent]): api.GreetingMessageChanged = {
    helloEvent.event match {
      case GreetingMessageChanged(msg) => api.GreetingMessageChanged(helloEvent.entityId, msg)
    }
  }
}