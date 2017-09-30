package models

case class GrantAuthority(requestedAuthorityId: String, scopes: Seq[Scope], application: EnvironmentApplication)
