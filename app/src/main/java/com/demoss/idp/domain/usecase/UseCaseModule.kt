package com.demoss.idp.domain.usecase

import org.koin.dsl.module.module

val useCaseModule = module {
    factory { CreateTestUseCase(get()) }
    factory { GetTestsUserCase(get()) }
    factory { GetTestUseCase(get()) }
    factory { UpdateTestUseCase(get()) }
    factory { DeleteTestUseCase(get()) }
}