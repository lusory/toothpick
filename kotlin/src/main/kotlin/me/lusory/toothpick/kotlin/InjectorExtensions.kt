package me.lusory.toothpick.kotlin

import me.lusory.toothpick.Injector
import kotlin.reflect.KClass

operator fun Injector.invoke(vararg modules: Any): Injector = Injector.of(modules)

operator fun Injector.invoke(modules: Collection<Any>): Injector = Injector.of(modules)

operator fun <T : Any> Injector.get(clazz: KClass<T>): T = instance(clazz.java)

operator fun <T : Any> Injector.get(clazz: KClass<T>, name: String): T = instance(clazz.java, name)

operator fun <T : Any> Injector.get(clazz: KClass<T>, qualifier: KClass<out Annotation>): T = instance(clazz.java, qualifier.java)