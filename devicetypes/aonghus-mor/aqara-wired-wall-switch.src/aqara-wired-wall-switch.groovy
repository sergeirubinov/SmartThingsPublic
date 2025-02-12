/**
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
 *  Modified from DTH by a4refillpad
 *
 *  01.10.2017 first release
 *  01.11.2018 Adapted the code to work with QBKG03LM
 *  21.04.2019 handling cluster 0006 to update the app device state when the buttons are pressed manually
 *             used code parts from: https://github.com/dschich/Smartthings/blob/master/devicetypes/dschich/Aqara-Switch-QBKG12LM.src/Aqara-Switch-QBKG12LM.groovy  
 *  20.06.2019 - 12.08.2020 modified by @aonghus-mor 
 *  12.08.2020 modified by @aonghus-mor to recognise QBKG21LM & QBKG22LM (but not yet QBKG25LM).
 *  13.10.2020 New version by @aonghus-mor for new smartthigs app.
 *  27.10.2020 Adapted for the new 3 button switch QBKG25LM ( Thanks to @Chiu for his help).
 *  09.03.2021 Extensive revision by @aonghus-mor, including own child DH.
*/
 
import groovy.json.JsonOutput
import physicalgraph.zigbee.zcl.DataType

metadata 
{
    definition (name: "Aqara Wired Wall Switch", namespace: "aonghus-mor", author: "aonghus-mor",
                mnmn: "SmartThingsCommunity", 
                vid: "822341f9-0eac-3dc8-b02a-fbdc64fd9541", 
                //vid: "c24838eb-ca6e-355f-a3c1-ce9b829365dc",
    			//mnmn: "LUMI", vid: "generic-switch", 
                ocfDeviceType: "oic.d.switch")
    {
        capability "Actuator"
        capability "Sensor"
        capability "Configuration"
        capability "Refresh"
        capability "Switch"
        capability "Momentary"
        capability "Button"
        capability "Temperature Measurement"
        capability "Health Check"
        capability "Power Meter"
        capability "Polling"
        
        command "childOn"
        command "childOff"
        command "childRefresh"
        
        attribute "lastCheckin", "string"
        attribute "lastPressType", "enum", ["soft","hard","both","held","released","refresh","double"]
        //attribute "momentary", "ENUM", ["Pressed", "Standby"]
        //attribute "button", "ENUM", ["Pressed", "Held", "Standby"]
        //attribute "tempOffset", "number"
   
        fingerprint profileId: "0104", deviceId: "0051", inClusters: "0000,0001,0002,0003,0004,0005,0006,0010,000A", outClusters: "0019,000A", 
        		manufacturer: "LUMI", model: "lumi.ctrl_neutral2", deviceJoinName: "Aqara Switch QBKG03LM"
        fingerprint profileId: "0104", deviceId: "0051", inClusters: "0000,0003,0001,0002,0019,000A", outClusters: "0000,000A,0019", 
                manufacturer: "LUMI", model: "lumi.ctrl_neutral1", deviceJoinName: "Aqara Switch QBKG04LM"
        fingerprint profileId: "0104", deviceId: "0051", inClusters: "0000,0003,0001,0002,0019,000A", outClusters: "0000,000A,0019", 
                manufacturer: "LUMI", model: "lumi.ctrl_ln1.aq1", deviceJoinName: "Aqara Switch QBKG11LM"      
        fingerprint profileId: "0104", deviceId: "0051", inClusters: "0000,0003,0001,0002,0019,000A", outClusters: "0000,000A,0019", 
                manufacturer: "LUMI", model: "lumi.ctrl_ln2.aq1", deviceJoinName: "Aqara Switch QBKG12LM"       
    	fingerprint profileId: "0104", deviceId: "0051", inClusters: "0000,0003,0001,0002,0019,000A", outClusters: "0000,000A,0019", 
                manufacturer: "LUMI", model: "lumi.switch.b1lacn02", deviceJoinName: "Aqara Switch QBKG21LM"
        fingerprint profileId: "0104", deviceId: "0051", inClusters: "0000,0003,0001,0002,0019,000A", outClusters: "0000,000A,0019", 
                manufacturer: "LUMI", model: "lumi.switch.b2lacn02", deviceJoinName: "Aqara Switch QBKG22LM"
        fingerprint profileId: "0104", deviceId: "0051", inClusters: "0000,0003,0001,0002,0019,000A", outClusters: "0000,000A,0019", 
                manufacturer: "LUMI", model: "lumi.switch.b1nacn02", deviceJoinName: "Aqara Switch QBKG23LM"
        fingerprint profileId: "0104", deviceId: "0051", inClusters: "0000,0003,0001,0002,0019,000A", outClusters: "0000,000A,0019", 
                manufacturer: "LUMI", model: "lumi.switch.b2nacn02", deviceJoinName: "Aqara Switch QBKG24LM"
        fingerprint profileId: "0104", deviceId: "0051", inClusters: "0000,0003,0001,0002,0019,000A", outClusters: "0000,000A,0019", 
                manufacturer: "LUMI", model: "lumi.switch.l3acn3", deviceJoinName: "Aqara Switch QBKG25LM" 
        fingerprint profileId: "0104", deviceId: "0051", inClusters: "0000,0003,0001,0002,0019,000A", outClusters: "0000,000A,0019", 
                manufacturer: "LUMI", model: "lumi.switch.n3acn3", deviceJoinName: "Aqara Switch QBKG26LM"   
     }
	
    preferences 
    {	
        input name: "unwired", type: "bool", title: "Is this switch unwired?", required: true, displayDuringSetup: true
        input name: "tempOffset", type: "decimal", title:"Temperature Offset", 
        							description:"Adjust temperature by this many degrees", range:"*..*", required: false, displayDuringSetup: false                         
        input name: "infoLogging", type: "bool", title: "Display info log messages?", required: false, displayDuringSetup: false
		input name: "debugLogging", type: "bool", title: "Display debug log messages?", required: false, displayDuringSetup: false
    }
}


// Parse incoming device messages to generate events
def parse(String description)
{
   	displayDebugLog( "Parsing '${description}'" )
    
    def dat = new Date()
    def newcheck = dat.time
    state.lastCheckTime = state.lastCheckTime == null ? 0 : state.lastCheckTime
    def diffcheck = newcheck - state.lastCheckTime
    //displayDebugLog(newcheck + " " + state.lastCheckTime + " " + diffcheck)
    state.lastCheckTime = newcheck
  
   	def events = []
   
   	if (description?.startsWith('catchall:')) 
		events = events + parseCatchAllMessage(description)
	else if (description?.startsWith('read attr -')) 
		events = events + parseReportAttributeMessage(description)
    else if (description?.startsWith('on/off: '))
        parseCustomMessage(description) 
   
    def now = dat.format("HH:mm:ss EEE dd MMM '('zzz')'", location.timeZone) + "\n" + state.lastPressType
    events << createEvent(name: "lastCheckin", value: now, descriptionText: "Check-In", displayed: debugLogging)
    
    displayDebugLog( "Parse returned: $events" )
    return events
}
/*
private def showFlags()
{
	return state.flag + " " + state.sw1 + " " + state.sw2 + " " + state.sw3 + " " + state.lastCheckTime
}
*/
def updateTemp()
{
	// every half hour get the temperature
    def dat = new Date()
    def cmd = null
    if ( dat.time - state.lastTempTime > 1800000 ) 
    {
    	log.debug "Requesting Temperature"
        state.lastTempTime = dat.time
        cmd = [response(delayBetween(zigbee.readAttribute(0x0002,0),1000))]
    }
	return cmd
}

private def parseCatchAllMessage(String description) 
{
	def cluster = zigbee.parse(description)
	displayDebugLog( cluster )
    def events = []
    
    switch ( cluster.clusterId ) 
    {
    	case 0x0000: 
         	if ( cluster.command == 0x0a && cluster.data[0] == 0x01 )
            {
        		Map dtMap = dataMap(cluster.data)
                displayDebugLog( "Map: " + dtMap )
                if ( ! state.numButtons )
                	getNumButtons()
                events = events + setTemp( dtMap.get(3) ) + ( dtMap.get(149) ? getWatts( dtMap.get(149) ) : [] )
                
                displayDebugLog("Number of Switches: ${state.numSwitches}")
                def onoff = (dtMap.get(100) ? "on" : "off")
                switch ( state.numSwitches )
                {
                	case 1:
                    	displayInfoLog( "Hardware Switch is ${onoff}" )
                        displayDebugLog( 'Software Switch is ' + device.currentValue('switch') )
                        break
                    case 2:
                    	def onoff2 = (dtMap.get(101) ? 'on' : 'off' )
                        //def child = getChild(2)
                    	def child = getChildDevices()[0]
                        displayDebugLog( "Unwired Switches: ${state.unwiredSwitches}" )
                		displayDebugLog( "Hardware Switches are (" + onoff + "," + onoff2 +")" )
                        displayDebugLog( 'Software Switches are (' + device.currentValue('switch') + ',' + child.device.currentValue('switch') + ')' )
                    	
                        break
                    case 3:
                    	def onoff2 = (dtMap.get(101) ? 'on' : 'off' )
                        def child2 = getChild(0)
                        def onoff3 = (dtMap.get(102) ? 'on' : 'off' )
                        def child3 = getChild(1)
                    	displayDebugLog( "Unwired Switches: ${state.unwiredSwitches}" )
                		displayDebugLog( "Hardware Switches are (${onoff}, ${onoff2}, ${onoff3})" )
                        displayDebugLog( 'Software Switches are (' + device.currentValue('switch') + ',' + child2.device.currentValue('switch') + ',' + child3.device.currentValue('switch')+ ')' )
                    	
                        break
                    	
                    default:
                    	displayDebugLog("Number of switches unrecognised: ${state.numSwitches}")
                }
            }
            else 
            {
        		//Map dtMap = dataMap(cluster.data)
                //displayDebugLog( "Map: " + dtMap )
                displayDebugLog('CatchAll ignored.')
            }
        	break
        case 0x0006: 	
            displayDebugLog('CatchAll message ignored.')
    }
    return events
}

private def setTemp(int temp)
{ 
    def event = []
    temp = temp ? temp : 0
    if ( state.tempNow != temp || state.tempOffset != tempOffset )
    {
      	state.tempNow = temp
        state.tempOffset = tempOffset ? tempOffset : 0
        if ( getTemperatureScale() != "C" ) 
            temp = celsiusToFahrenheit(temp)
        state.tempNow2 = temp + state.tempOffset     
        event << createEvent(name: "temperature", value: state.tempNow2, unit: getTemperatureScale())
        displayDebugLog("Temperature is now ${state.tempNow2}°")          	
	}
    displayDebugLog("setTemp: ${event}")
    return event
}

private def getWatts(float pwr)
{
	def event = []
    pwr = pwr ? pwr : 0.0
    if ( abs( pwr - (float)state.power ) > 1e-4 )
    {	
    	state.power = (float)pwr
    	event << createEvent(name: 'power', value: pwr, unit: 'W')
    }
    displayDebugLog("Power: ${pwr} W")
	return event
}

private def abs(x) { return ( x > 0 ? x : -x ) } 

private def parseReportAttributeMessage(String description) 
{
	Map descMap = (description - "read attr - ").split(",").inject([:]) { map, param ->
		def nameAndValue = param.split(":")
		map += [(nameAndValue[0].trim()):nameAndValue[1].trim()]
     }
	 def events = []
    
    switch (descMap.cluster) 
    {
    	case "0000":
        	displayDebugLog( "Basic Cluster: $descMap" )
            if ( descMap.attrId == "0007" && descMap.value != "03" )
            	state.batteryPresent = false
            break
    	case "0001": //battery
        	if ( descMap.value == "0000" )
            	state.batteryPresent = false
        	else if (descMap.attrId == "0020")
				events = events + getBatteryResult(convertHexToInt(descMap.value / 2))
            break
 		case "0002": // temperature
        	if ( descMap.attrId == '0000' ) 
            	events = events + setTemp( convertHexToInt(descMap.value) )
            break
 		case "0006":  //button press
        	events = events + parseSwitchOnOff(descMap)
            break
        case "000C": //analog input
        	if ( descMap.attrID == "0055" )
            {
            	int x = Integer.parseInt(descMap.value, 16)
            	float y = Float.intBitsToFloat(x)
                events = events + getWatts(y)
            }
        	//displayDebugLog("Power: ${y} Watts")
        	break
        case "0012": //Multistate Input
        	state.flag = 'hard'
            //parsePressed(descMap)
   			displayDebugLog("Cluster 0x0012 seen for hard press.")
            break
 		//case "0008":
        //	if ( descMap.attrId == "0000")
    	//		event = createEvent(name: "switch", value: "off")
        //    break
 		default:
        	displayDebugLog( "unknown cluster in $descMap" )
    }
	return events
}

def parseSwitchOnOff(Map descMap)
{
	//parse messages on read attr cluster 0x0006
	
    state.flag = ( descMap.value[1] == 'c' ) ? 'double' : 'hard' 
    def events = []
    int endp = descMap.endpoint.toInteger()
    int endpcode = state.endpoints.indexOf(endp)
    state.lastEndpcode = endpcode
	if ( endpcode > 2 || state.flag == 'double' )
    {
    	def action = state.flag == 'double' ? 'double' : 'pushed'
    	switch ( endpcode )
        {
        	case 0:
            case 3:
            	events << createEvent( name: 'button', value: action, data:[buttonNumber: 1], isStateChange: true)
                break
            case 1..2:
            case 4..5:
            	int idx = endpcode - ( endpcode < 3 ? 1 : 4 )
                Map button = [name: 'button', value: action, data:[buttonNumber: 1], isStateChange: true]
            	getChild(idx).sendEvent( button)
                displayDebugLog("Child ${idx+1}   ${button}")
                break
            case 6:
            	events << createEvent(name: 'button', value: 'pushed', data:[buttonNumber: 2], isStateChange: true )
                break
            default:
            	displayDebugLog("Invalid read attr code")
        }
     }
     else if ( !state.unwiredSwitches[endpcode] )
     {
     	def onoff = descMap.value[-1] == "1" ? "on" : "off"
     	switch ( endpcode )
        {
        	case 0:
            	events << createEvent( name: 'switch', value: onoff, isStateChange: true)
                break
            case 1..2:
            	Map sw = [name: 'switch', value: onoff, isStateChange: true]
            	getChild(endpcode-1).sendEvent( sw)
                displayDebugLog("{Child ${endpcode}   ${sw}")
                break
            default:
            	displayDebugLog("invalid rad attr code")
        }
    } 
    else
    	displayDebugLog("read attr endpoint ${endp} ignored.")   	
	return events
}

private def parseCustomMessage(String description) 
{
	displayDebugLog( "Parsing Custom Message: $description" )
    if (description == 'on/off: 0')
    {
    	state.holdDone = false
    	runIn( 1, doHoldButton )  // delay 1 second to make sure 'double' has been identified before implementing 'held'
    }
    else if (description == 'on/off: 1')
        doHoldButton()
}

def doHoldButton()
{
	displayDebugLog("doHoldButton   Hold Done: ${state.holdDone}")
    if ( !state.holdDone )  // avoid this function being called twice.
   	{
        state.holdDone = true
        if ( state.flag != 'double' )
        {
            Map button = [name: 'button', value: 'held', data:[buttonNumber: 1], isStateChange: true]
            switch( state.lastEndpcode )
            {
                case 0:
                //events << createEvent( button )
                sendEvent(button)
                displayDebugLog(button)
                break
                case 1:
                case 2:
                getChild(state.lastEndpcode-1).sendEvent( button )
                displayDebugLog("Child ${state.lastEndpcode}  ${button}")
                break
                default:
                    displayDebugLog("Unexpected custom message")
            }
        }
    }
}

private def getChild(int i)
{
    def children = getChildDevices()
    def child
    if ( children.size() == 1 )
        child = children[0]
    else
    {
    	def idx = state.childDevices[i]	
    	for (child1 in children)
    	{	
        	if ( child.deviceNetworkId == idx )
			{
            	child = child1
				break
            }
        }
    }
    return child
}

def childOn(String dni) 
{
    int idx = state.childDevices.indexOf(dni) + 1
    int endp = state.endpoints[idx]
    def cmd = state.unwiredSwitches[idx] ? [] : zigbee.command(0x0006, 0x01, "", [destEndpoint: endp] )
    displayDebugLog("ChildOn ${dni}  ${idx}  ${cmd}" )
    cmd 
}

def childOff(String dni) 
{
 	int idx = state.childDevices.indexOf(dni) + 1
    int endp = state.endpoints[idx]
    def cmd = state.unwiredSwitches[idx] ? [] : zigbee.command(0x0006, 0x00, "", [destEndpoint: endp] )
    displayDebugLog( "ChildOff ${dni}  ${idx}  ${cmd}")
    cmd 
}

private def childFromNetworkId(String dni)
{
	def child
	def children = getChildDevices()
    if (children.size()  == 1)
    	child = children[0]
    else
    {
    	for ( child1 in children )
    	{
        	if ( child1.deviceNetworkId == dni )
            {
            	child = child1
                break
            }
        }
	}
	return child
}

def childRefresh(String dni, Boolean unw) 
{
    displayInfoLog("Child Refresh: ${dni}")
    def child = childFromNetworkId(dni)
    def idx = state.childDevices.indexOf(dni)
    state.unwiredSwitches[idx] = unw   
    displayDebugLog("Child Refresh: ${idx} ${child.deviceNetworkId}   ${state.unwiredSwitches}")
	refresh()
}

def on() 
{
    displayDebugLog("Switch 1 pressed on")
    Map button = [name: 'button', value: 'pushed', data:[buttonNumber: 1], isStateChange: true]
    sendEvent(button)
    displayDebugLog(button)
    def cmd = []
    if ( state.unwiredSwitches[0] )
    {
    	Map swoff = [name: 'switch', value: 'off']
    	sendEvent( swoff )
        displayDebugLog(swoff)
    }
    else
    	cmd = zigbee.command(0x0006, 0x01, "", [destEndpoint: state.endpoints[0]] )
    displayDebugLog( cmd )
    return cmd 
}

def off() 
{
    displayDebugLog("Switch 1 pressed off")
    Map button = [name: 'button', value: 'pushed', data:[buttonNumber: 1], isStateChange: true]
    sendEvent( button )
    displayDebugLog(button)
    def cmd = state.unwiredSwitches[0] ? [] : zigbee.command(0x0006, 0x00, "", [destEndpoint: state.endpoints[0]] )
    displayDebugLog( cmd )
    cmd
}

private def clearState()
{
	displayDebugLog(state)
    def unwiredSwitches = state.unwiredSwitches
    def tempNow = state.tempNow
    def tempNow2 = state.tempNow2
    state = null
    state.unwiredSwitches = unwiredSwitches
    state.tempNow = tempNow
    state.tempNow2 = tempNow2
    displayDebugLog(state)
}

def refresh() 
{
	displayInfoLog( "refreshing" )
    clearState()
    def dat = new Date()
    state.lastTempTime = dat.time
   	displayDebugLog(settings)
    
    //state.unwired = parseUnwiredSwitch()
    state.tempNow = state.tempNow == null ? 0 : state.tempNow
    state.tempNow2 = state.tempNow2 == null ? 0 : state.tempNow2
    state.tempOffset = tempOffset == null ? 0 : tempOffset
    
    //state.final = 'off'
    displayDebugLog(state.unwiredSwitches)
    if ( state.unwiredSwitches == null )
        state.unwiredSwitches = [unwired]
    else
        state.unwiredSwitches[0] = unwired
        
    getNumButtons()
    if ( state.numSwitches > 1 )
    {
    	def childDevices = getChildDevices()
		displayDebugLog("Children: ${childDevices}: ${childDevices.size()}")
        /*try 
        {
        	displayDebugLog("Deleting Children")
            for ( child in childDevices )
            	deleteChildDevice("${child.deviceNetworkId}")
    	} 
        catch(Exception e) 
        { 
        	displayDebugLog("${e}") 
        }
    	childDevices = getChildDevices()
        */
   		if (childDevices.size() == 0) 
    	{
			displayInfoLog( "Creating Children" )
            //state.childDevices = [device.name]
			try 
    		{
    			if ( state.numSwitches > 1)
    			{
                	state.childDevices = []
                	for ( int i = 1; i < state.numSwitches; i++ )
                    {
                    	def networkId = "${device.deviceNetworkId}-${i}"
    					addChildDevice( "Aqara Wired Wall Switch Child", networkId , null,[label: "${device.displayName}-(${i})"])  
                        state.childDevices[i-1] = networkId
                    }
                }
			} 
        	catch(Exception e) 
        	{
				displayDebugLog( "${e}")
        	}
			displayInfoLog("Child created")
		}    
        else
        {	
        	state.childDevices = []
            childDevices = getChildDevices()
            if ( state.numSwitches == 2 )
             	state.childDevices[0] = childDevices[0].deviceNetworkId
			else
            {
            	if ( childDevices[0].deviceNetworkId[-1].toInteger() < childDevices[1].deviceNetworkId[-1].toInteger() )
                {
                	state.childDevices[0] = childDevices[0].deviceNetworkId
                    state.childDevices[1] = childDevices[1].deviceNetworkId
                }
                else
                {
                	state.childDevices[0] = childDevices[1].deviceNetworkId
                    state.childDevices[1] = childDevices[0].deviceNetworkId
                }
            }
        }
        displayDebugLog(state.childDevices)
        
        displayDebugLog(state.unwiredSwitches)
        childDevices = getChildDevices()
    	for (child in childDevices)
    	{	
            child.sendEvent(name: 'checkInterval', value: 3000)
            displayDebugLog("${child}  ${child.deviceNetworkId}")
		}
    }    
    displayDebugLog("Devices: ${state.childDevices}")
    displayDebugLog("Unwired Switches: ${state.unwiredSwitches}")
    
    sendEvent(name: 'supportedButtonValues', value: ['pushed', 'held', 'double'], isStateChange: true)
    sendEvent( name: 'checkInterval', value: 3000, data: [ protocol: 'zigbee', hubHardwareId: device.hub.hardwareID ] )
    
    //state.unwiredSwitches = [unwired]
    def cmds = 	zigbee.readAttribute(0x0001, 0) + 
    			zigbee.readAttribute(0x0002, 0)
    /*
   
        	cmds += zigbee.writeAttribute(0x0000, 0xFF22, DataType.UINT8, (state.unwired & 0x01) != 0x00 ? 0xFE : 0x12, [mfgCode: "0x115F"]) +
                		//zigbee.writeAttribute(0x0000, 0xFF22, DataType.UINT8,   true ? 0xFE : 0x12, [mfgCode: "0x115F"]) +
                		zigbee.writeAttribute(0x0000, 0xFF23, DataType.UINT8, (state.unwired & 0x02) != 0x00 ? 0xFE : 0x22, [mfgCode: "0x115F"]) +
                		["delay 1000"] + zigbee.readAttribute(0x0000, 0xFF22, [mfgCode: "0x115F"]) + zigbee.readAttribute(0x0000, 0xFF23, [mfgCode: "0x115F"])
                        break
     */  
	displayDebugLog("State: ${state}")
     displayDebugLog( cmds )
     //updated()
     state.flag = null
     cmds
}

def installed()
{
	displayDebugLog('installed')
    refresh()
}

def configure()
{
	displayDebugLog('configure')
	refresh()
}

def updated()
{
	displayDebugLog('updated')
	refresh()
}

def ping()
{
	displayDebugLog("Pinged")
    zigbee.readAttribute(0x0002, 0)
}

def poll()
{
	displayDebugLog("Polled")
    zigbee.readAttribute(0x0002, 0)
}

private getNumButtons()
{
    String model = device.getDataValue("model")
    switch ( model ) 
    {
    	case "lumi.ctrl_neutral1": //QBKG04LM
        case "lumi.switch.b1lacn02": //QBKG21LM
        	state.numSwitches = 1
     		state.numButtons = 1
            state.endpoints = [0x02,0xF2,0xF3,0x04,0xF4,0xF5,0xF6]
            break
        case "lumi.ctrl_ln1.aq1": //QBKG11LM
        	state.numSwitches = 1
     		state.numButtons = 1
            state.endpoints = [0x01,0xF2,0xF3,0x04,0xF4,0xF5,0xF6]
			break
		case "lumi.switch.b1nacn02": //QBKG23LM
            state.numSwitches = 1
     		state.numButtons = 1
            state.endpoints = [0x01,0xF2,0xF3,0x05,0xF4,0xF5,0xF6]
            break
        case "lumi.ctrl_neutral2": //QBKG03LM
        	state.numSwitches = 2
        	state.numButtons = 2
            state.endpoints = [0x02,0x03,0xF3,0x04,0x05,0xF5,0x06]
            break
        case "lumi.switch.b2lacn02": //QBKG22LM 
        	state.numSwitches = 2
        	state.numButtons = 2
            state.endpoints = [0x02,0x03,0xF3,0x2A,0x2B,0xF5,0x06]
            break
        case "lumi.ctrl_ln2.aq1": //QBKG12LM      
        case "lumi.switch.b2nacn02": //QBKG24LM
           	state.numSwitches = 2
        	state.numButtons = 2
            state.endpoints = [0x01,0x02,0xF3,0x05,0x06,0xF5,0xF6]
            break
        case "lumi.switch.l3acn3": //QBKG25LM
        case "lumi.switch.n3acn3": //QBKG26LM
            state.numSwitches = 3
            state.numButtons = 4
            state.endpoints = [0x01,0x02,0x03,0x29,0x02A,0x2B,0xF6]
            break
        default:
        	displayDebugLog("Unknown device model: " + model)
    }
    displayDebugLog("endpoints: ${state.endpoints}")
    sendEvent(name: 'numberOfButtons', value: state.numButtons, displayed: false )
    displayDebugLog( "Setting Number of Buttons to ${state.numButtons}" )
}

private Integer convertHexToInt(hex) 
{
	int result = Integer.parseInt(hex,16)
    return result
}

private Map dataMap(data)
{
	// convert the catchall data from check-in to a map.
	Map resultMap = [:]
	int maxit = data.size()
    int it = 4
    while ( it < maxit )
    {
    	int lbl = 0x00000000 | data.get(it)
        byte type = data.get(it+1)
        switch ( type)
       	{
        	case DataType.BOOLEAN: 
            	resultMap.put(lbl, (boolean)data.get(it+2))
                it = it + 3
                break
            case DataType.UINT8:
            	resultMap.put(lbl, (short)(0x0000 | data.get(it+2)))
                it = it + 3
                break
            case DataType.UINT16:
            	resultMap.put(lbl, (int)(0x00000000 | (data.get(it+3)<<8) | data.get(it+2)))
                it = it + 4
                break
            case DataType.UINT32:
            	long x = 0x0000000000000000
                for ( int i = 0; i < 4; i++ )
              		x |= data.get(it+i+2) << 8*i
            	resultMap.put(lbl, x )
                it = it + 6
                break
              case DataType.UINT40:
            	long x = 0x000000000000000
                for ( int i = 0; i < 5; i++ )
              		x |= data.get(it+i+2) << 8*i
            	resultMap.put(lbl, x )
                it = it + 7
                break  
            case DataType.UINT64:
            	long x = 0x0000000000000000
                for ( int i = 0; i < 8; i++ )
                	x |= data.get(it+i+2) << 8*i
            	resultMap.put(lbl, x )
                it = it + 10
                break 
            case DataType.INT8:
            	resultMap.put(lbl, (short)(data.get(it+2)))
                it = it + 3
                break
             case DataType.INT16:
            	resultMap.put(lbl, (int)((data.get(it+3)<<8) | data.get(it+2)))
                it = it + 4
                break
            case DataType.FLOAT4:
                int x = 0x00000000 
                for ( int i = 0; i < 4; i++ ) 
                	x |= data.get(it+i+2) << 8*i
                float y = Float.intBitsToFloat(x) 
            	resultMap.put(lbl,y)
                it = it + 6
                break
            default: displayDebugLog( "unrecognised type in dataMap: " + zigbee.convertToHexString(type) )
            	return resultMap
        }
    }
    return resultMap
}

private def displayDebugLog(message) 
{
	if (debugLogging)
		log.debug "${device.displayName} ${message}"
}

private def displayInfoLog(message) 
{
	//if (infoLogging || state.prefsSetCount < 3)
    if (infoLogging)
		log.info "${device.displayName} ${message}"
}