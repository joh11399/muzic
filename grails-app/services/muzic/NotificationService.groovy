package muzic

import grails.converters.JSON
import grails.events.Listener

class NotificationService {

  static transactional = false

  @Listener
  def songPlayed(def message) {
    def id = (JSON.parse(message as String) as Map).id as Long
    Play play = Play.get(id)
    if (!play) {
      log.error("Invalid message received for Play with id:${id}")
      return
    }

    def messageText = "Your tracked artist ${play.song.artist.name} had a song play (${play.song.title})!"
    Follow.findAllByArtist(play.song.artist).each { Follow follow ->
        ProfileMessage.withTransaction {
          ProfileMessage profileMessage = new ProfileMessage(text: messageText, timestamp: new Date())
          follow.profile.addToMessages(profileMessage)
          log.info("Saving profile message for ${follow.profile.email}")
          profileMessage.save(flush: true, failOnError: true)
        }
    }

    return null
  }
}
