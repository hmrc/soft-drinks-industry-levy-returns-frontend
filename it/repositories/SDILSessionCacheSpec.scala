package repositories

import org.scalatest.wordspec.AsyncWordSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.mockito.MockitoSugar
import org.mockito.Mockito._
import play.api.libs.json._
import scala.concurrent.Future


class SDILSessionCacheSpec extends AsyncWordSpec with Matchers with MockitoSugar {

  implicit val intFormat: Format[Int] = Format(Reads.IntReads, Writes.IntWrites)

  private val repo    = mock[SDILSessionCacheRepository]
  private val cascade = mock[CascadeUpsert]
  private val cache   = new SDILSessionCache(repo, cascade)

  "save" should {
    "upsert when cache exists" in {
      val enrol    = "E1"
      val existing = CacheMap(enrol, Map("a" -> JsString("x")))
      val updated  = CacheMap(enrol, Map("a" -> JsString("x"), "k" -> JsNumber(1)))

      when(repo.get(enrol)).thenReturn(Future.successful(Some(existing)))
      when(cascade.apply("k", 1, existing)).thenReturn(updated)
      when(repo.upsert(updated)).thenReturn(Future.successful(true))

      cache.save(enrol, "k", 1).map { res =>
        res shouldBe true
        verify(repo).get(enrol)
        verify(cascade).apply("k", 1, existing)
        val _ = verify(repo).upsert(updated)
        succeed
      }
    }

    "create new cache when none exists" in {
      val enrol   = "E2"
      val base    = CacheMap(enrol, Map.empty)
      val updated = CacheMap(enrol, Map("k" -> JsNumber(2)))

      when(repo.get(enrol)).thenReturn(Future.successful(None))
      when(cascade.apply("k", 2, base)).thenReturn(updated)
      when(repo.upsert(updated)).thenReturn(Future.successful(true))

      cache.save(enrol, "k", 2).map { res =>
        res shouldBe true
        verify(repo).get(enrol)
        verify(cascade).apply("k", 2, base)
        val _ = verify(repo).upsert(updated)
        succeed
      }
    }
  }

  "remove" should {
    "return false if cache missing" in {
      when(repo.get("E3")).thenReturn(Future.successful(None))
      cache.remove("E3", "k").map { r =>
        r shouldBe false
        succeed
      }
    }

    "remove key and upsert when present" in {
      val enrol    = "E4"
      val existing = CacheMap(enrol, Map("k" -> JsString("v"), "keep" -> JsString("y")))
      val expected = CacheMap(enrol, Map("keep" -> JsString("y")))

      when(repo.get(enrol)).thenReturn(Future.successful(Some(existing)))
      when(repo.upsert(expected)).thenReturn(Future.successful(true))

      cache.remove(enrol, "k").map { res =>
        res shouldBe true
        val _ = verify(repo).upsert(expected)
        succeed
      }
    }
  }

  "removeRecord" should {
    "delegate to repository" in {
      when(repo.removeRecord("E5")).thenReturn(Future.successful(true))
      cache.removeRecord("E5").map { r =>
        r shouldBe true
        succeed
      }
    }
  }

  "fetch / fetchEntry" should {
    "fetch returns cache when present" in {
      val cm = CacheMap("E6", Map("x" -> JsNumber(10)))
      when(repo.get("E6")).thenReturn(Future.successful(Some(cm)))
      cache.fetch("E6").map { got =>
        got shouldBe Some(cm)
        succeed
      }
    }

    "fetchEntry returns Some(value) when key exists" in {
      val cm = CacheMap("E7", Map("n" -> JsNumber(42)))
      when(repo.get("E7")).thenReturn(Future.successful(Some(cm)))

      cache.fetchEntry[Int]("E7", "n").map { got =>
        got shouldBe Some(42)
        succeed
      }
    }

    "fetchEntry returns None when key absent or cache missing" in {
      when(repo.get("E8")).thenReturn(Future.successful(Some(CacheMap("E8", Map.empty))))
      when(repo.get("E9")).thenReturn(Future.successful(None))
      for {
        a <- cache.fetchEntry[Int]("E8", "absent")
        b <- cache.fetchEntry[Int]("E9", "anything")
      } yield {
        a shouldBe None
        b shouldBe None
        succeed
      }
    }
  }

  "extendSession" should {
    "update lastUpdated via repository" in {
      when(repo.updateLastUpdated("E10")).thenReturn(Future.successful(true))
      cache.extendSession("E10").map { r =>
        r shouldBe true
        succeed
      }
    }
  }
}
