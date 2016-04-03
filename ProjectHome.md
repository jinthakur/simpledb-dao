This is a DAO support library which provide basic operations for items stored in Simple DB. It used typica project for Simple DB API calls.
It allows you to work with SimpleDB in JPA style.

  * allow to use some JPA annotations in your model (@Entity, @Table, @Column)

  * provide basic operations with your model entities:
    * store entity
    * delete entity
    * get entity by id
    * get portion of entities
    * get portion of entities filtered by query
    * get all entities filtered by query
    * count entities in domain
    * count entities filtered by query in domain

Dependencies:
  * typica (http://code.google.com/p/typica/)
  * common-beanutils (http://commons.apache.org/beanutils/)
  * commons-lang (http://commons.apache.org/lang/)
  * ejb3-persistence.jar
  * log4j (http://logging.apache.org/)
