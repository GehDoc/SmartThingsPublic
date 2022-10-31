/**
 *  Copyright 2022 Gérald Hameau
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 *  Keep Me Dry
 *
 *  Author: Gérald Hameau
 */
definition(
  name: "Keep me dry",
  namespace: "GehDoc",
  author: "Gérald Hameau",
  description: "Fan should switch on to dry the room, only if possible regarding the surrounding humidity level.",
  category: "Green Living",
  iconUrl: "https://s3.amazonaws.com/smartapp-icons/Meta/temp_thermo@.png",
  iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Meta/temp_thermo@2x.png"
)

preferences {
  section("Target this % of humidity in the room") {
    input "humiditySensor1", "capability.relativeHumidityMeasurement", title: 'Monitored sensor', required: true
    input "humidityMin", "number", range: "0..100", title: "turn off the fan when <", required: true, defaultValue: 54
    input "humidityMax", "number", range: "0..100", title: "turn on the fan when >=", required: true, defaultValue: 56
  }
  section("Cap the targeted % of humidity") {
  	paragraph "The targeted is not reachable, if the surrounding % of humidity is higher.\
		So, let the fan stops when (monitored + offsetMin) < reference.\
    	And, restarts when (monitored + offsetMax) >= reference."
    input "humiditySensorReference", "capability.relativeHumidityMeasurement", title: 'Reference sensor', required: true
    input "referenceHumidityOffsetMin", "number", range: "0..100", title: "Min % offset", required: true, defaultValue: 4
    input "referenceHumidityOffsetMax", "number", range: "0..100", title: "Max % offset", required: true, defaultValue: 5
  }
  section("Control the fan") {
    input "switch1", "capability.switch", title: 'Fan control switch', required: true
  }
}

def initialize() {
  subscribe(humiditySensor1, "humidity", humidityHandler)
  subscribe(humiditySensorReference, "humidity", humidityHandler)
}

def installed() {
  log.debug "${app.label} installed with settings: ${settings}"
  initialize()
}

def updated() {
  log.debug "${app.label} updated with settings: ${settings}"
  unsubscribe()
  initialize()
}

def humidityHandler(evt) {
  def currentHumidity = Integer.valueOf(humiditySensor1.currentValue('humidity'))
  def referenceHumidityMin = Integer.valueOf(humiditySensorReference.currentValue('humidity')) + referenceHumidityOffsetMin
  def referenceHumidityMax = Integer.valueOf(humiditySensorReference.currentValue('humidity')) + referenceHumidityOffsetMax
  
  log.debug "Current humidity: ${currentHumidity}"
  log.debug "Reference humidity: ${referenceHumidity}"
  log.debug "Target: ${humidityMin}, Max: ${humidityMax}"
  log.debug "Caped to : ${referenceHumidityMin}, Max: ${referenceHumidityMax}"
  
  if (currentHumidity >= humidityMax && currentHumidity >= referenceHumidityMax) {
    log.debug "Current humidity Rose Above ${humidityMax} and ${referenceHumidity}: activating ${switch1}"
    switch1.on()
  }

  if (currentHumidity < humidityMin || currentHumidity < referenceHumidityMin) {
    log.debug "Current humidity Fell Below ${humidityMin} or ${referenceHumidity}: disabling ${switch1}"
    switch1.off()
  }
}