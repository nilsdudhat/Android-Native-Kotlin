package com.udemy.dependency.injection.app.dagger.modules

import com.udemy.dependency.injection.app.Constants
import com.udemy.dependency.injection.app.services.EmailService
import com.udemy.dependency.injection.app.services.MessageService
import com.udemy.dependency.injection.app.services.NotificationService
import dagger.Module
import dagger.Provides
import javax.inject.Named

@Module
class NotificationServiceModule {

    /*@Named(Constants.EMAIL)
    @Binds
    abstract fun getEmailService(emailService: EmailService): NotificationService*/

    @Named(Constants.EMAIL)
    @Provides
    fun getEmailService(): NotificationService {
        return EmailService()
    }

    /*@Named(Constants.MESSAGE)
    @Binds
    abstract fun getMessageService(messageService: MessageService): NotificationService*/

    @Named(Constants.MESSAGE)
    @Provides
    fun getMessageService(retryCount: Int): NotificationService {
        return MessageService(retryCount)
    }
}