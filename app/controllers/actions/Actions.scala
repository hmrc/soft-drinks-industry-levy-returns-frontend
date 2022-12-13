package controllers.actions

import com.google.inject.Inject
import play.api.mvc.{ActionBuilder, AnyContent}
import models.requests.IdentifierRequest

class Actions @Inject()(
                         identify: IdentifierAction,
                       ) {
    def auth : ActionBuilder[IdentifierRequest, AnyContent] = identify
}
