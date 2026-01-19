TestMidiConnector : UnitTest {
	var widget;

	setUp {
		CVWidget.initMidiOnStartUp = false;
		widget = CVWidgetKnob(\test);
		CVWidget.wmc.midiSources.m.value_(('source 1': 12345, 'source 2': 54321)).changedPerformKeys(CVWidget.syncKeys);
	}

	tearDown {
		widget.remove;
		CVWidget.wmc.midiSources.m.value_(()).changedPerformKeys(CVWidget.syncKeys);
	}

	test_new {
		var connector2, connector3;
		var vals;
		connector2 = MidiConnector(widget);
		this.assertEquals(widget.wmc.midiConnectors.m.value.size, 2, "After creating another MidiConnector the widget hold two MidiConnectors in widget.midiConnectors");
		connector3 = MidiConnector(widget, "c2");
		this.assertEquals(widget.wmc.midiConnectors.m.value.size, 3, "After creating another MidiConnector the widget hold three MidiConnectors in widget.midiConnectors");
		this.assertEquals(connector3.name, "c2", "The third MidiConnector should have been named 'c2'");
		vals = widget.wmc.midiOptions.m.value.collect { |v|
			v == (
				midiMode: 0,
				midiZero: 64,
				midiResolution: 1,
				snapDistance: 0.1,
				ctrlButtonGroup: 1,
				midiInputMapping: (mapping: \linlin)
			);
		};
		this.assertEquals(vals, [true, true, true], "The values of midiOptions model declared within the MidiConnectors should default to an Event (midiMode: 0, midiZero: 63, midiResolution: 1, snapDistance: 0.1, ctrlButtonGroup: 1, midiInputMapping: (mapping: 'linlin'))");
		vals = widget.wmc.midiDisplay.m.value.collect { |v|
			v == (
				src: 'source...',
				chan: "chan",
				ctrl: "ctrl",
				learn: "L",
				toolTip: "Click and move hardware slider/knob to connect to"
			)
		};
		this.assertEquals(vals, [true, true, true], "The values of midiDisplay model declared within the MidiConnectors should default to an Event (src: \"source\", chan: \"chan\", ctrl: \"ctrl\", learn: \"L\", toolTip: \"Click and move hardware slider/knob to connect to\")");
	}

	test_name {
		this.assertEquals(widget.wmc.midiConnectors.m.value[0].name, widget.wmc.midiConnectorNames.m.value[0], "On creation a CVWidgetKnob should have one MideiConnector named 'MIDI Connection 1'. This name is held in the widget's midiConnectorNames model value at index 0.");
		widget.wmc.midiConnectors.m.value[0].name_('xxxx');
		this.assertEquals(widget.wmc.midiConnectors.m.value[0].name, widget.wmc.midiConnectorNames.m.value[0], "After renaming the MidiConnector the name returned by calling the method 'name' should be equal the widget's midiConnectorNames model value at index 0.");
	}

	test_remove {
		var connector2 = widget.addMidiConnector;
		this.assertEquals([
			widget.wmc.midiConnectorNames.m.value,
			widget.wmc.midiConnections.m.value,
			widget.wmc.midiOptions.m.value,
			widget.wmc.midiDisplay.m.value
		].collectAs({ |m| m.size }, Set), Set[2], "After adding MidiConnector connector2 to widget models related to the widget's connector should hold values of size 2.");
		widget.wmc.midiConnectors.m.value[0].remove;
		this.assertEquals(widget.wmc.midiConnectors.m.value, List[connector2], "After removing the widget's connector at index 0 in widget.wmc.midiConnectors.m.value widget.wmc.midiConnectors.m.value should hold a single connector, connector2, at in widget.wmc.midiConnectors.m.value.");
		this.assertEquals([
			widget.wmc.midiConnectorNames.m.value,
			widget.wmc.midiConnections.m.value,
			widget.wmc.midiOptions.m.value,
			widget.wmc.midiDisplay.m.value
		].collectAs({ |m| m.size }, Set), Set[1], "After removing the MidiConnector at widget.wmc.midiConnectors.m.value[0] widget models related to the widget's connector should hold values of size 1.");
		widget.wmc.midiConnectors.m.value[0].remove;
		this.assertEquals(widget.wmc.midiConnectors.m.value.size, 1, "After calling 'remove' on the last remaining MidiConnector in widget.wmc.midiConnectors.m.value widget.wmc.midiConnectors.m.value.size should still return 1.");
		widget.wmc.midiConnectors.m.value[0].remove(true);
		this.assertEquals(widget.wmc.midiConnectors.m.value.size, 0, "After calling 'remove' with arg 'forceAll' set to true on the last remaining MidiConnector in widget.wmc.midiConnectors.m.value widget.wmc.midiConnectors.m.value.size should return 0.");
	}

	test_midiConnect_disconnect {
		var connector1 = widget.wmc.midiConnectors.m.value[0];
		var connector2 = widget.addMidiConnector;

		connector1.midiConnect(num: 2, chan: 0, srcID: 12345, argTemplate: 3);
		connector2.midiConnect(num: 3);
		this.assertEquals(widget.wmc.midiConnections.m.value[0].class, MIDIFunc, "After connecting a widget's default MidiConnector instance to control nr. 2, channel 0 and source ID 12345 widget.wmc.midiConnections.m.value[0] hold a MIDIFunc");
		this.assertEquals(widget.wmc.midiConnections.m.value[0].srcID, 12345, "widget.wmc.midiConnections.m.value[0].srcID should return 12345");
		this.assertEquals(widget.wmc.midiConnections.m.value[0].chan, 0, "widget.wmc.midiConnections.m.value[0].chan should return 0");
		this.assertEquals(widget.wmc.midiConnections.m.value[0].msgNum, 2, "widget.wmc.midiConnections.m.value[0].msgNum should return 2");
		this.assertEquals(widget.wmc.midiConnections.m.value[0].argTemplate, 3, "widget.wmc.midiConnections.m.value[0].argTemplate should return 3");
		this.assertEquals(widget.wmc.midiDisplay.m.value[0].learn, "X", "widget.wmc.midiDisplay.m.value[0].learn should equal \"X\"");
		this.assertEquals(widget.wmc.midiDisplay.m.value[0].src, 12345, "widget.wmc.midiDisplay.m.value[0].src should equal 12345");
		this.assertEquals(widget.wmc.midiDisplay.m.value[0].chan, 0, "widget.wmc.midiDisplay.m.value[0].chan should equal 0");
		this.assertEquals(widget.wmc.midiDisplay.m.value[0].ctrl, 2, "widget.wmc.midiDisplay.m.value[0].ctrl should equal 2");
		this.assertEquals(widget.wmc.midiDisplay.m.value[0].template, 3, "widget.wmc.midiDisplay.m.value[0].template should equal 3");
		this.assertEquals(widget.wmc.midiDisplay.m.value[0].dispatcher.class, MIDIMessageDispatcher, "widget.wmc.midiDisplay.m.value[0].dispatcher.class should equal MIDIMessageDispatcher");
		this.assertEquals(widget.wmc.midiDisplay.m.value[0].toolTip, "Click to disconnect", "widget.wmc.midiDisplay.m.value[0].template should equal \"Click to disconnect\"");
		connector1.midiDisconnect;
		this.assertEquals(widget.wmc.midiConnections.m.value[0], nil, "After disconnecting a widget's default MidiConnector instance widget.wmc.midiConnections.m.value[0] should hold nil");
		this.assertEquals(widget.wmc.midiDisplay.m[0].value, (ctrl: "ctrl", chan: "chan", src: 'source...', learn: "L", toolTip: "Click and move hardware slider/knob to connect to"), "After disconnecting a widget's default MidiConnector instance widget.wmc.midiDisplay.m[0].value should hold an Event with the default values: (ctrl: \"ctrl\", chan: \"chan\", src: \"source\", learn: \"L\", toolTip: \"Click and move hardware slider/knob to connect to\")");
		connector1.remove;
		this.assertEquals(widget.wmc.midiConnectors.m.value.size, 1, "After removing connector1 widget.wmc.midiConnectors.m.value should hold one MidiConnector.");
		this.assertEquals(widget.wmc.midiConnections.m.value[0].class, MIDIFunc, "After calling connection2.midiConnect(num: 3) and calling connection1.remove widget.wmc.midiConnections.m.value[0] should hold a MIDIFunc");
	}
}

TestOscConnector : UnitTest {
	var widget;

	setUp {
		widget = CVWidgetKnob(\test);
	}

	tearDown {
		widget.remove;
	}

	test_new {
		var connector2, connector3;
		var vals;
		connector2 = OscConnector(widget);
		this.assertEquals(widget.wmc.oscConnectors.m.value.size, 2, "After creating another OscConnector the widget hold two OscConnectors in widget.oscConnectors");
		connector3 = OscConnector(widget, "c2");
		this.assertEquals(widget.wmc.oscConnectors.m.value.size, 3, "After creating another OscConnector the widget hold three OscConnectors in widget.oscConnectors");
		this.assertEquals(connector3.name, "c2", "The third OscConnector should have been named 'c2'");
		vals = widget.wmc.oscOptions.m.value.collect { |v|
			v == (
				oscEndless: false,
				oscResolution: 1,
				oscCalibration: true,
				oscSnapDistance: 0.1,
				oscInputRange: [0.0001, 0.0001],
				oscInputMapping: (mapping: \linlin),
				oscMatching: false
			);
		};
		this.assertEquals(vals, [true, true, true], "The values of oscOptions model declared within the OscConnectors should default to an Event (oscEndless: false, oscResolution: 1, oscCalibration: true, oscSnapDistance: 0.1, oscInputRange: [0.0001, 0.0001], oscInputMapping: (mapping: 'linlin'))");
		vals = widget.wmc.oscDisplay.m.value.collect { |v|
			v == (
				ipField: nil,
				portField: nil,
				nameField: '/my/cmd/name',
				index: 1,
				connectorButVal: 0,
				connect: "learn"
			)
		};
		this.assertEquals(vals, [true, true, true], "The values of oscDisplay model declared within the OscConnectors should default to an Event (ipField: nil, portField: nil, nameField: '/my/cmd/name', index: 1, connectorButVal: 0, connect: \"Learn\")");
	}

	test_name {
		this.assertEquals(widget.wmc.oscConnectors.m.value[0].name, widget.wmc.oscConnectorNames.m.value[0], "On creation a CVWidgetKnob should have one MideiConnector named 'MIDI Connection 1'. This name is held in the widget's midiConnectorNames model value at index 0.");
		widget.wmc.midiConnectors.m.value[0].name_('xxxx');
		this.assertEquals(widget.wmc.oscConnectors.m.value[0].name, widget.wmc.oscConnectorNames.m.value[0], "After renaming the MidiConnector the name returned by calling the method 'name' should be equal the widget's midiConnectorNames model value at index 0.");
	}

	test_remove {
		var connector2 = widget.addOscConnector;
		this.assertEquals([
			widget.wmc.oscConnectorNames.m.value,
			widget.wmc.oscConnections.m.value,
			widget.wmc.oscOptions.m.value,
			widget.wmc.oscDisplay.m.value
		].collectAs({ |m| m.size }, Set), Set[2], "After adding OscConnector connector2 to widget models related to the widget's connector should hold values of size 2.");
		widget.wmc.oscConnectors.m.value[0].remove;
		this.assertEquals(widget.wmc.oscConnectors.m.value, List[connector2], "After removing the widget's connector at index 0 in widget.wmc.oscConnectors.m.value widget.wmc.oscConnectors.m.value should hold a single connector, connector2, at in widget.wmc.oscConnectors.m.value.");
		this.assertEquals([
			widget.wmc.oscConnectorNames.m.value,
			widget.wmc.oscConnections.m.value,
			widget.wmc.oscOptions.m.value,
			widget.wmc.oscDisplay.m.value
		].collectAs({ |m| m.size }, Set), Set[1], "After removing the OscConnector at widget.wmc.oscConnectors.m.value[0] widget models related to the widget's connector should hold values of size 1.");
		widget.wmc.oscConnectors.m.value[0].remove;
		this.assertEquals(widget.wmc.oscConnectors.m.value.size, 1, "After calling 'remove' on the last remaining OscConnector in widget.wmc.oscConnectors.m.value widget.wmc.oscConnectors.m.value.size should still return 1.");
		widget.wmc.oscConnectors.m.value[0].remove(true);
		this.assertEquals(widget.wmc.oscConnectors.m.value.size, 0, "After calling 'remove' with arg 'forceAll' set to true on the last remaining OscConnector in widget.wmc.oscConnectors.m.value widget.wmc.oscConnectors.m.value.size should return 0.");
	}

	test_oscConnect_disconnect {
		var connector1 = widget.wmc.oscConnectors.m.value[0];
		var connector2 = widget.addOscConnector;

		connector1.oscConnect(NetAddr.localAddr, '/test1', 1, argTemplate: 4);
		connector2.oscConnect(NetAddr.localAddr, '/test2', 1, matching: true);
		this.assertEquals(widget.wmc.oscConnections.m.value[0].class, OSCFunc, "After connecting a widget's default OscConnector instance widget.wmc.oscConnections.m.value[0] should hold an OSCFunc.");
		this.assertEquals(widget.wmc.oscConnections.m.value[0].srcID, NetAddr.localAddr, "widget.wmc.oscConnections.m.value[0].srcID should return NetAddr.localAddr.");
		this.assertEquals(widget.wmc.oscConnections.m.value[0].path, '/test1', "widget.wmc.oscConnections.m.value[0].path should return '/test1'.");
		this.assertEquals(widget.wmc.oscConnections.m.value[0].recvPort, nil, "widget.wmc.oscConnections.m.value[0].recvPort should return nil.");
		this.assertEquals(widget.wmc.oscConnections.m.value[0].argTemplate, [0, 1, 2, 3], "widget.wmc.oscConnections.m.value[0].argTemplate should return [0, 1, 2, 3].");
	}
}