package main.scala

import pureconfig._
import pureconfig.generic.auto._
import scala.util.Try

case class Team1(id: String, process: Boolean, emails: Seq[String], urls: Map[String, String])

object Team1 {
  implicit val approach1: ConfigReader[Team1] = {
    case class RawTeam1(id: String, process: String, emails: String, urls: String)
    // In the real world I'd use emap and define better error messages on failure
    implicitly[ConfigReader[RawTeam1]].map { r =>
      Team1(
        r.id,
        r.process.toBoolean,
        r.emails.split(",").toSeq,
        r.urls.split(";").toSeq.map { s => 
          val Array(k,v) = s.split(":")
          k -> v
        }.toMap
      )
    }
  }

  def main(a: Array[String]): Unit = {
    case class Conf(team: Team1)
    Env.apply()
    println(pureconfig.loadConfig[Conf])
  }
}

case class Process(value: Boolean) extends AnyVal
object Process {
  // You could also you use the ConfigCursor API instead of `fromNonEmptyStringTry`
  implicit val configReader = ConfigReader.fromNonEmptyStringTry(s => Try(Process(s.toBoolean)))
}
case class Emails(value: Seq[String]) extends AnyVal
object Emails {
  implicit val configReader = ConfigReader.fromNonEmptyStringTry(s => Try(Emails(s.split(","))))
}
case class Urls(value: Map[String, String]) extends AnyVal
object Urls {
  implicit val configReader = ConfigReader.fromNonEmptyStringTry(s => Try(Urls(
    s.split(";").toSeq.map { s => 
      val Array(k,v) = s.split(":")
      k -> v
    }.toMap)))
}

case class Team2(id: String, process: Process, emails: Emails, urls: Urls)

object Team2 {

  def main(a: Array[String]): Unit = {
    case class Conf(team: Team2)
    Env.apply()
    println(pureconfig.loadConfig[Conf])
  }

}

object Env {
  def apply(): Unit = Map(
    "TEAM_ID" -> "jimmy",
    "TEAM_PROCESS" -> "true",
    "TEAM_EMAILS" -> "a@b.c,d@e.f",
    "TEAM_URLS" -> "a:b;c:d"
  ).foreach((System.setProperty _).tupled)
}
