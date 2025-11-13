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
			nameField: '/my/cmd/name',
			index: 1,
			connectorButVal: 0,
			editEnabled: true
		), "widget1.wmc.oscDisplay.m.value[0] should initially be ('editEnabled': true, 'nameField': /my/cmd/name, 'index': 1, 'connectorButVal': 0)");
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
			nameField: '/my/cmd/name',
			index: 1,
			connectorButVal: 0,
			editEnabled: true
		), "After setting oscv1.e.ipselect to '192.168.1.2' widget1.wmc.oscDisplay.m.value[0] should be ('ipField': '192.168.1.2', 'editEnabled': true, 'nameField': /my/cmd/name, 'index': 1, 'connectorButVal': 0)");
		this.assertEquals(oscv1.e.portselect.items, ['port...', 3214, 42560], "After calling oscv1.e.ipselect.valueAction_(oscv1.e.ipselect.items.indexOf('192.168.1.2')) oscv1.e.portselect.items should be ['port...', 3214, 42560]");
		this.assertEquals(oscv1.e.cmdselect.items, ['cmd name...', '/3214/1', '/3214/2', '/42560/1', '/42560/2'], "After calling oscv1.e.ipselect.valueAction_(oscv1.e.ipselect.items.indexOf('192.168.1.2')) oscv1.e.cmdselect.items should be ['cmd name...', '/3214/1', '/3214/2', '/42560/1', '/42560/2']");
		oscv1.e.portselect.valueAction_(1);
		this.assertEquals(oscv1.e.cmdselect.items, ['cmd name...', '/3214/1', '/3214/2'], "After calling oscv1.e.portselect.valueAction_(1) oscv1.e.cmdselect.items should be ['cmd name...', '/3214/1', '/3214/2']");
		this.assertEquals(widget1.wmc.oscDisplay.m.value[0], (
			ipField: '192.168.1.2',
			portField: 3214,
			nameField: '/my/cmd/name',
			index: 1,
			connectorButVal: 0,
			editEnabled: true
		), "After setting oscv1.e.portselect to 3214 widget1.wmc.oscDisplay.m.value[0] should be ('ipField': '192.168.1.2', portField: 3214, 'editEnabled': true, 'nameField': /my/cmd/name, 'index': 1, 'connectorButVal': 0)");
		oscv2 = OscSelectsComboView(widget: widget1);
		this.assertEquals(oscv2.e.ipselect.items, ['IP address...', '192.168.1.2', '192.168.1.3'], "After creating another OscSelectsComboView instance for widget1 its IP select items should hold ['IP address...', '192.168.1.2', '192.168.1.3'].");
		this.assertEquals(oscv2.e.ipselect.value, 1, "After creating another OscSelectsComboView instance for widget1 its IP select's value should be 1.");
		this.assertEquals(oscv2.e.portselect.items, ['port...', 3214, 42560], "After creating another OscSelectsComboView instance for widget1 its portselect items should be ['port...', 3214, 42560]");
		this.assertEquals(oscv2.e.portselect.value, 1, "After creating another OscSelectsComboView instance for widget1 its portselect's value should equal 1");
		this.assertEquals(oscv2.e.cmdselect.items, ['cmd name...', '/3214/1', '/3214/2'], "After creating another OscSelectsComboView instance for widget1 its cmdselect items should be ['cmd name...', '/3214/1', '/3214/2']");
		oscv2.close;
	}

	test_index_ {}

	test_widget_ {}

	test_close {}
}