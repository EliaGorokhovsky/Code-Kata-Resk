package com.fairviewcodeclub.resk

import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

/**
 * A controller that handles running test environments
 */
@RestController
@RequestMapping(value="/test/api")
class TestController {


    /**
     * Takes in a key and creates a test environment for that team
     * If a test environment already exists, it gets replaced/reset with a new one
     */
    //@RequestMapping(method=[RequestMethod.POST], params=["key"])

    /**
     * Takes in a request to act
     */
    //@RequestMapping(method=[RequestMethod.POST], params=["action", "key"])

    /**
     * Returns the game state of the environment matching the given key
     */
    //@RequestMapping(method=[RequestMethod.GET])

    /**
     * Gets the score and snek details for the test env of the given key
     */
    //@RequestMapping(path = ["/progress"], method = [RequestMethod.GET])

}