package controllers

import com.google.inject.Inject
import models.{User, UserWithId}
import play.api.mvc.{AbstractController, ControllerComponents}

class UserController @Inject()(cc: ControllerComponents) extends AbstractController(cc) {
  def loginForm = Action {
    Ok(views.html.index("Your new application is ready."))
  }

  def registerForm = Action {
    Ok(views.html.index("Your new application is ready."))
  }

  def updateForm = Action {
    Ok(views.html.index("Your new application is ready."))
  }

  def login(user: User) = Action {
    Ok(views.html.index("Your new application is ready."))
  }

  def register(user: User) = Action {
    Ok(views.html.index("Your new application is ready."))
  }

  def update(userWithId: UserWithId) = Action {
    Ok(views.html.index("Your new application is ready."))
  }
}
