TestOscSelectsComboView : UnitTest {
	var widget1, widget2, oscv1, oscv2;

	setUp {
		widget1 = CVWidgetKnob(\t1);
		oscv1 = OscSelectsComboView(widget: widget1);
		OSCCommands.ipsAndCmds.putAll(('192.168.1.2': ('3214': ('/3214/1': 1, '/3214/2': 1), '42560': ('/42560/1': 1, '/42560/2': 1)), '192.168.1.3': ('31269': ('/31269/1': 1, '/31269/2': 1))));
	}

	tearDown {
		oscv1.close;
		widget1.remove;
		OSCCommands.ipsAndCmds.clear;
		OSCCommands.collectSync(false);
	}

	test_new {
		this.assert(OscSelectsComboView.all[widget1][0] === oscv1, "OscSelectsComboView.all[widget1] should hold a list with a single OscSelectsComboView instance.");
		this.assertEquals(widget1.syncKeys, [\default, OscSelectsComboView.asSymbol], "The widget's 'syncKeys' should contain two Symbols, 'default' and 'OscSelectsComboView', after creating a new OscSelectsComboView.");
		this.assertEquals(CVWidget.syncKeys, [\default, OscSelectsComboView.asSymbol], "CVWidget.syncKeys should hold two Symbols, 'default' and 'OscSelectsComboView', after creating a new OscSelectsComboView.");
		this.assert(oscv1.connector === widget1.wmc.oscConnectors.m.value[0], "The OscSelectsComboView's connector should be identical with the connector at the widget's oscConnectors at index 0.");
		this.assertEquals(oscv1.e.ipselect.items, ['IP address...'], "The PopUpMenu for selecting an IP address should hold a single default value 'IP address...'");
		this.assertEquals(oscv1.e.portselect.items, ['port...'], "The PopUpMenu for selecting a port should hold a single default value 'port...'");
		this.assertEquals(oscv1.e.cmdselect.items, ['cmd name...'], "The PopUpMenu for selecting a command name should hold a single default value 'cmd name...'");
		this.assertEquals(widget1.wmc.oscDisplay.m.value[0], (
			ipField: nil,
			portField: nil,
			nameField: '/path/to/cmd',
			index: 1,
			connectorButVal: 0,
			connect: "Learn"
		), "widget1.wmc.oscDisplay.m.value[0] should initially be ('nameField': /path/to/cmd, 'index': 1, 'connectorButVal': 0, 'connect': \"Learn\")");
		OSCCommands.collectSync(false);
		this.assert(oscv1.e.ipselect.items.size == 3, "The PopUpMenu for selecting an IP address should hold three values after syncing addresses and commands through calling OSCCommands.collectSync(false)");
		this.assertEquals(oscv1.e.ipselect.items, ['IP address...', '192.168.1.2', '192.168.1.3'], "The PopUpMenu for selecting an IP address should hold 'IP address...', '192.168.1.2' and '192.168.1.3'");
		this.assertEquals(oscv1.e.portselect.items, ['port...'], "The PopUpMenu for selecting a port should still hold a single default value 'port...' after calling OSCCommands.collectSync(false)");
		this.assert(oscv1.e.cmdselect.items.size == 7,"The PopUpMenu for selecting a command name should still hold seven values after calling OSCCommands.collectSync(false)");
		this.assertEquals(oscv1.e.cmdselect.items, ['cmd name...', '/31269/1', '/31269/2', '/3214/1', '/3214/2', '/42560/1', '/42560/2'], "The PopUpMenu for selecting a command name should hold the values 'cmd name...', '/31269/1', '/31269/2', '/3214/1', '/3214/2', '/42560/1' and '/42560/2'.");
		oscv1.e.ipselect.valueAction_(1);
		this.assertEquals(widget1.wmc.oscDisplay.m.value[0], (
			ipField: '192.168.1.2',
			portField: nil,
			nameField: '/path/to/cmd',
			index: 1,
			connectorButVal: 0,
			connect: "Learn"
		), "After setting oscv1.e.ipselect to '192.168.1.2' widget1.wmc.oscDisplay.m.value[0] should be ('ipField': '192.168.1.2', 'editEnabled': true, 'nameField': /path/to/cmd, 'index': 1, 'connectorButVal': 0)");
		this.assertEquals(oscv1.e.portselect.items, ['port...', 3214, 42560], "After calling oscv1.e.ipselect.valueAction_(oscv1.e.ipselect.items.indexOf('192.168.1.2')) oscv1.e.portselect.items should be ['port...', 3214, 42560]");
		this.assertEquals(oscv1.e.cmdselect.items, ['cmd name...', '/3214/1', '/3214/2', '/42560/1', '/42560/2'], "After calling oscv1.e.ipselect.valueAction_(oscv1.e.ipselect.items.indexOf('192.168.1.2')) oscv1.e.cmdselect.items should be ['cmd name...', '/3214/1', '/3214/2', '/42560/1', '/42560/2']");
		oscv1.e.portselect.valueAction_(1);
		this.assertEquals(oscv1.e.cmdselect.items, ['cmd name...', '/3214/1', '/3214/2'], "After calling oscv1.e.portselect.valueAction_(1) oscv1.e.cmdselect.items should be ['cmd name...', '/3214/1', '/3214/2']");
		this.assertEquals(widget1.wmc.oscDisplay.m.value[0], (
			ipField: '192.168.1.2',
			portField: 3214,
			nameField: '/path/to/cmd',
			index: 1,
			connectorButVal: 0,
			connect: "Learn"
		), "After setting oscv1.e.portselect to 3214 widget1.wmc.oscDisplay.m.value[0] should be ('ipField': '192.168.1.2', portField: 3214, 'editEnabled': true, 'nameField': /path/to/cmd, 'index': 1, 'connectorButVal': 0)");
		oscv2 = OscSelectsComboView(widget: widget1);
		this.assertEquals(oscv2.e.ipselect.items, ['IP address...', '192.168.1.2', '192.168.1.3'], "After creating another OscSelectsComboView instance for widget1 its IP select items should hold ['IP address...', '192.168.1.2', '192.168.1.3'].");
		this.assertEquals(oscv2.e.ipselect.value, 1, "After creating another OscSelectsComboView instance for widget1 its IP select's value should be 1.");
		this.assertEquals(oscv2.e.portselect.items, ['port...', 3214, 42560], "After creating another OscSelectsComboView instance for widget1 its portselect items should be ['port...', 3214, 42560]");
		this.assertEquals(oscv2.e.portselect.value, 1, "After creating another OscSelectsComboView instance for widget1 its portselect's value should equal 1");
		this.assertEquals(oscv2.e.cmdselect.items, ['cmd name...', '/3214/1', '/3214/2'], "After creating another OscSelectsComboView instance for widget1 its cmdselect items should be ['cmd name...', '/3214/1', '/3214/2']");
		oscv2.close;
	}

	test_index_ {
		// hmmm...
		OSCCommands.collectSync(false);
		widget1.addOscConnector;
		this.assertEquals(oscv1.e.ipselect.items, ['IP address...', '192.168.1.2', '192.168.1.3'], "After instantiation an OscSelectsComboView's IP select should contain all IP addresses currently held CVWidget's oscAddrAndCmds model prepended by 'IP address...'.");
		oscv1.index_(1);
		this.assertEquals(oscv1.e.ipselect.items, ['IP address...', '192.168.1.2', '192.168.1.3'], "After switching the connector by calling index_ on an OscSelectsComboView an OscSelectsComboView's IP select should still contain all IP addresses currently held CVWidget's oscAddrAndCmds model prepended by 'IP address...'.");
		this.assert(oscv1.connector === widget1.wmc.oscConnectors.m.value[1], "After adding an OscConnector to the widget and calling oscv1.index_(1) oscv's 'connector' variable should be identical with the widget's OscConnector at index 1");
		widget1.addOscConnector;
		oscv2 = OscSelectsComboView(widget: widget1, connectorID: 2);
		this.assert(oscv2.connector === widget1.wmc.oscConnectors.m.value[2], "After adding another OscConnector to the widget and creating OscSelectsComboView oscv2 with arg 'connectorID' set to 2' ms2's 'connector' variable should be identical with the widget's OscConnector at index 2.");
		oscv1.e.ipselect.valueAction_(1);
		this.assertEquals(oscv2.e.portselect.items, ['port...'], "After setting oscv1.e.ipselect.value to 1 oscv2.e.portselect.items should equal ['port...']");
		oscv2.index_(1);
		this.assertEquals(oscv2.e.portselect.items, ['port...', 3214, 42560], "After setting oscv1.e.ipselect.value to 1 and calling oscv2.index_(1) oscv2.e.portselect.items should equal ['port...', 3214, 42560]");
		oscv1.index_(2);
		this.assert(oscv1.e.ipselect.value == 0, "After calling oscv1.index_(2) of oscv1.e.ipselect.value should return 0");
		oscv1.e.ipselect.valueAction_(1);
		oscv1.e.portselect.valueAction_(2);
		oscv2.index_(2);
		this.assertEquals(oscv2.e.cmdselect.items, ['cmd name...', '/42560/1', '/42560/2'], "After calling oscv1.index_(2), oscv1.e.ipselect.valueAction_(1), oscv1.e.portselect.valueAction_(2) and calling oscv2.index_(2) oscv2's portselect items should equal ['cmd name...', '/42560/1', '/42560/2'].");
		oscv2.close;
	}

	test_widget_ {
		OSCCommands.collectSync(false);
		widget2 = CVWidgetKnob(\t2);
		oscv1.e.ipselect.valueAction_(1);
		oscv1.e.portselect.valueAction_(2);
		oscv1.e.cmdselect.valueAction_(2);
		oscv1.widget_(widget2);

		this.assert(oscv1.widget === widget2, "After calling widget_ on the OscSelectsComboView with arg 'widget' set to widget2 the OscSelectsComboView's widget getter should return widget2");
		this.assert(oscv1.e.ipselect.value == 0 and: {
			oscv1.e.portselect.value == 0 and: {
				oscv1.e.cmdselect.value == 0
			}
		}, "The OscSelectsComboView's PopUpMenus 'ipselect', 'portselect' and 'cmdselect' should all have been set to value 0");
		widget2.remove;
	}

	test_close {
		oscv2 = OscSelectsComboView(widget: widget1);
		oscv1.close;
		this.assertEquals(widget1.syncKeys, [\default, OscSelectsComboView.asSymbol], "After closing OscSelectsComboView oscv1 widget.syncKeys should hold 2 symbols: 'default' and 'OscSelectsComboView'.");
		oscv2.close;
		this.assertEquals(widget1.syncKeys, [\default], "After closing OscSelectsComboView oscv2 widget.syncKeys one symbol 'default' should have remained in widget.syncKeys.");
	}
}