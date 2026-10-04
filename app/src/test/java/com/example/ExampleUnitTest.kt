package com.example

import com.example.model.CameraMode
import com.example.network.GeminiCameraService
import com.example.util.SampleScenesProvider
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun testHeuristicAdvicePortrait() {
    val advice = GeminiCameraService.generateHeuristicDirectorAdvice(null, CameraMode.PORTRAIT)
    assertNotNull(advice)
    assertTrue(advice.compositionScore in 80..100)
    assertEquals(2.0f, advice.zoomFactor, 0.01f)
    assertNotNull(advice.targetBoundingBox)
    assertTrue(advice.actionableSteps.isNotEmpty())
  }

  @Test
  fun testSampleScenesAvailable() {
    val scenes = SampleScenesProvider.sampleScenes
    assertTrue(scenes.isNotEmpty())
    val portraitScene = scenes.firstOrNull { it.category == CameraMode.PORTRAIT }
    assertNotNull(portraitScene)
  }
}

