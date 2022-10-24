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
  iconUrl: "https://s3.amazonaws.com/smartapp-icons/Meta/temp_thermo.png",
  iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Meta/temp_thermo@2x.png"
)

preferences {
  section("Aim for this humidity percentage at this location") {
    input "humiditySensor1", "capability.relativeHumidityMeasurement", title: 'Monitored sensor', required: true
    input "humidityMin", "number", title: "Target % of humidity", required: true
    input "humidityMax", "number", title: "Turn on the fan from that % of humidity", required: true
  }
  section("The target humidity percentage cannot be lower than") {
    input "humiditySensorReference", "capability.relativeHumidityMeasurement", title: 'Reference sensor', required: true
    input "referenceHumidityTreshold", "number", title: "Tolerate that % offset (target + offset > reference)", required: true
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
  def referenceHumidity = Integer.valueOf(humiditySensorReference.currentValue('humidity')) + referenceHumidityTreshold
  
  log.debug "Current humidity: ${currentHumidity}"
  log.debug "Reference humidity: ${referenceHumidity}"
  log.debug "Min: ${humidityMin}, Max: ${humidityMax}"
  
  if (currentHumidity >= humidityMax && currentHumidity > referenceHumidity) {
    log.debug "Current humidity Rose Above ${humidityMax} and ${referenceHumidity}: activating ${switch1}"
    switch1.on()
  }

  if (currentHumidity <= humidityMin || currentHumidity <= referenceHumidity) {
    log.debug "Current humidity Fell Below ${humidityMin} or ${referenceHumidity}: disabling ${switch1}"
    switch1.off()
  }
}