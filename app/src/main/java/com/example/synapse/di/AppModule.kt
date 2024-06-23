package com.example.synapse.di

import com.example.synapse.model.DITest
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class AppModule {

    @Provides
    @Singleton
    fun getDiTestModel() : DITest{
        return DITest("test for di")
    }
}