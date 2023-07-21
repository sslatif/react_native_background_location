//
//  VoiceChangerModule.swift
//  sample
//
//  Created by Saqib on 14/07/2023.
//

import Foundation
import AVFoundation

@objc(VoiceChangerModule)
class VoiceChangerModule: NSObject {
  
  var player: AVAudioPlayer?
  
  var engine: AVAudioEngine!
  var file: AVAudioFile!
  var audioFile: AVAudioFile!
  
  @objc
  func changeVoiceToAlien() {
    engine = AVAudioEngine()
    guard let url = Bundle.main.url(forResource: "sound_sample", withExtension: "mp3") else { return }
    do {
      try self.audioFile = AVAudioFile(forReading: url)
    } catch {
      print("Error in audioFile")
    }
    
    print("changeVoiceToAlien")
    //showToast(controller: self, message : "changeVoiceToAlien", seconds: 2.0)
    playModifiedSound(value: -1000, rateOrPitch: "pitch")
  }
  
  @objc
  func changeVoiceToChild() {
    engine = AVAudioEngine()
    guard let url = Bundle.main.url(forResource: "sound_sample", withExtension: "mp3") else { return }
    do {
      try self.audioFile = AVAudioFile(forReading: url)
    } catch {
      print("Error in audioFile")
    }
     
    print("changeVoiceToChild")
    //showToast(controller: self, message : "changeVoiceToChild", seconds: 2.0)
    playModifiedSound(value: 1500, rateOrPitch: "pitch")
  }
  
  @objc
  func speedUpVoice() {
    engine = AVAudioEngine()
    guard let url = Bundle.main.url(forResource: "sound_sample", withExtension: "mp3") else { return }
    do {
      try self.audioFile = AVAudioFile(forReading: url)
    } catch {
      print("Error in audioFile")
    }
    playModifiedSound(value: 1.5, rateOrPitch: "rate")
  }
  
  @objc
  func slowDownVoice() {
    engine = AVAudioEngine()
    guard let url = Bundle.main.url(forResource: "sound_sample", withExtension: "mp3") else { return }
    do {
      try self.audioFile = AVAudioFile(forReading: url)
    } catch {
      print("Error in audioFile")
    }
    playModifiedSound(value: 0.5, rateOrPitch: "rate")
  }
  
  func playModifiedSound(value: Float, rateOrPitch: String){
          let audioPlayerNode = AVAudioPlayerNode()
          
          audioPlayerNode.stop()
          engine.stop()
          engine.reset()
          
          engine.attach(audioPlayerNode)
          
          let changeAudioUnitTime = AVAudioUnitTimePitch()
          
          if (rateOrPitch == "rate") {
              changeAudioUnitTime.rate = value
          } else {
              changeAudioUnitTime.pitch = value
          }
          
          engine.attach(changeAudioUnitTime)
          engine.connect(audioPlayerNode, to: changeAudioUnitTime, format: nil)
          engine.connect(changeAudioUnitTime, to: engine.outputNode, format: nil)
          audioPlayerNode.scheduleFile(audioFile, at: nil, completionHandler: nil)
          do {
            try engine.start()
          } catch {
            print("Error")
          }
          
          audioPlayerNode.play()
  }

  func showToast(controller: UIViewController, message : String, seconds: Double) {
    let alert = UIAlertController(title: nil, message: message, preferredStyle: .alert)
    alert.view.backgroundColor = UIColor.black
    alert.view.alpha = 0.6
    alert.view.layer.cornerRadius = 15

    controller.present(alert, animated: true)

    DispatchQueue.main.asyncAfter(deadline: DispatchTime.now() + seconds) {
        alert.dismiss(animated: true)
    }
}
  
  @objc
  static func requiresMainQueueSetup() -> Bool {
    return true
  }
}
