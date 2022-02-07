package de.matthiaskainer.acme.services

import de.matthiaskainer.acme.respositories.UserRepository
import org.springframework.stereotype.Component

@Component
class UserService(val userRepository: UserRepository) {
  fun all() = userRepository.getAll()
  fun getById(id: String) = userRepository.getById(id)
}
