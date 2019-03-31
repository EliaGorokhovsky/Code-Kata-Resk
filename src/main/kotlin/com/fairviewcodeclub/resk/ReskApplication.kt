package com.fairviewcodeclub.resk

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class ReskApplication

fun main(args: Array<String>) {
    runApplication<ReskApplication>(*args)
}
