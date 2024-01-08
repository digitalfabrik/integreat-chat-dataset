package org.tuerantuer.annotation.database

import kotlinx.datetime.toJavaInstant
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.transactions.transaction
import org.tuerantuer.annotation.models.Annotation

fun insertAnnotation(questionId: Int, annotation: Annotation) = transaction {
    insertAnnotation(QuestionEntity[questionId].id, annotation)
}

fun insertAnnotation(questionId: EntityID<Int>, annotation: Annotation) = transaction {
    // Every user should only be allowed to do one annotation per question
    // If a user does a second annotation for a question, it is to correct the previous one
    val previousAnnotations = AnnotationEntity.find {
        (Annotations.questionId eq questionId) and
                (Annotations.user eq annotation.user) and
                (Annotations.archived eq false)
    }
    AnnotationEntity.new {
        this.questionId = questionId
        answerLines = Json.encodeToString(annotation.answerLines)
        poor = annotation.poor
        user = annotation.user
        created = annotation.created.toJavaInstant()
    }
    previousAnnotations.forEach { it.archived = true }
}

fun getAnnotationsCount(): Int = transaction {
    return@transaction AnnotationEntity.find { Annotations.archived eq false }.count().toInt()
}