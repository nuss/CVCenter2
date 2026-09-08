TestMidiConnectorMS : UnitTest {
	var widget;

	setUp {
		CVWidget.initMidiOnStartUp = false;
		widget = CVWidgetMS(\test);
		CVWidget.wmc.midiSources.m.value_(('source 1': 12345, 'source 2': 54321)).changedPerformKeys(CVWidget.syncKeys);
	}

	tearDown {
		widget.remove;
		CVWidget.wmc.midiSources.m.value_(()).changedPerformKeys(CVWidget.syncKeys);
	}

	test_new {
		var connector2, connector3;
		var vals;
		connector2 = MidiConnectorMS(widget, slot: 0);
		this.assertEquals(widget.wmc.midiConnectors.m.collect { |slot| slot.value.size }, [2, 1, 1, 1, 1], "After creating another MidiConnector for slot 0 of the CVWidgetMS the widget should hold 2 MidiConnectors in slot 0 of widget.midiConnectors");
		connector3 = MidiConnectorMS(\test, "c2", 0);
		this.assertEquals(widget.wmc.midiConnectors.m.collect { |slot| slot.value.size }, [3, 1, 1, 1, 1], "After creating another MidiConnector for slot 0 of the CVWidgetMS the widget should hold 3 MidiConnectors in slot 0 of widget.midiConnectors");
		this.assertEquals(connector3.name, "c2", "The third MidiConnector in slot 0 of the CVWidgetMS should have been named 'c2'");
		vals = widget.wmc.midiOptions.m[0].value.collect { |v|
			v == (
				midiMode: 0,
				midiZero: 64,
				midiResolution: 1,
				snapDistance: 0,
				ctrlButtonGroup: 1,
				midiInputMapping: (mapping: \linlin)
			);
		};
		this.assertEquals(vals, [true, true, true], "The values of midiOptions model declared within the MidiConnectors in slot 0 should default to an Event (midiMode: 0, midiZero: 64, midiResolution: 1, snapDistance: 0, ctrlButtonGroup: 1, midiInputMapping: (mapping: 'linlin'))");
		vals = widget.wmc.midiDisplay.m[0].value.collect { |v|
			v == (
				src: 'source...',
				chan: "chan",
				ctrl: "ctrl",
				learn: "L",
				toolTip: "Click and move hardware slider/knob to connect to",
				slotToolTip: "Select the CVWidgetMS's '%' slot (widget has % slots)."
			)
		};
		this.assertEquals(vals, [true, true, true], "The values of midiDisplay model declared within the MidiConnectors in slot 0 should default to an Event (src: \"source\", chan: \"chan\", ctrl: \"ctrl\", learn: \"L\", toolTip: \"Click and move hardware slider/knob to connect to\", slotToolTip: \"Select the CVWidgetMS's '%' slot (widget has % slots).\")");
	}

	test_name {
		this.assertEquals(widget.wmc.midiConnectors.m.collect { |slot| slot.value[0].name }, widget.wmc.midiConnectorNames.m.collect(_.value).collect(_[0]), "On creation a CVWidgetMS should have one MidiConnectorMS named 'MIDI Connection 1'. This name is held in the widget's midiConnectorNames model value at index 0 in each slot.");
		widget.wmc.midiConnectors.m[0].value[0].name_('xxxx');
		this.assertEquals(widget.wmc.midiConnectors.m.collect { |slot| slot.value[0].name }, widget.wmc.midiConnectorNames.m.collect(_.value).collect(_[0]), "After renaming the MidiConnectorMS in slot 0 the name returned by calling the method 'name' should be equal the widget's midiConnectorNames model value in slot 0 at index 0.");
	}

	test_remove {
		var connector2 = widget.addMidiConnector(slot: 0);
		this.assertEquals([
			widget.wmc.midiConnectorNames.m[0].value,
			widget.wmc.midiConnections.m[0].value,
			widget.wmc.midiOptions.m[0].value,
			widget.wmc.midiDisplay.m[0].value
		].collectAs({ |m| m.size }, Set), Set[2], "After adding MidiConnectorMS connector2 to widget models in slot 0 the widget.midiConnectors[0] should hold values of size 2.");
		widget.wmc.midiConnectors.m[0].value[0].remove;
		this.assertEquals(widget.wmc.midiConnectors.m[0].value, List[connector2], "After removing the widget's connector at index 0 in widget.wmc.midiConnectors.m[0].value widget.wmc.midiConnectors.m[0].value should hold a single connector, connector2.");
		this.assertEquals([
			widget.wmc.midiConnectorNames.m[0].value,
			widget.wmc.midiConnections.m[0].value,
			widget.wmc.midiOptions.m[0].value,
			widget.wmc.midiDisplay.m[0].value
		].collectAs({ |m| m.size }, Set), Set[1], "After removing the MidiConnectorMS at widget.wmc.midiConnectors.m[0].value[0] widget models related to the widget's connector should hold values of size 1.");
		widget.wmc.midiConnectors.m[0].value[0].remove;
		this.assertEquals(widget.wmc.midiConnectors.m[0].value.size, 1, "After calling 'remove' on the last remaining MidiConnectorMS in widget.wmc.midiConnectors.m[0].value widget.wmc.midiConnectors.m[0].value.size should still return 1.");
		widget.wmc.midiConnectors.m[0].value[0].remove(true);
		this.assertEquals(widget.wmc.midiConnectors.m[0].value.size, 0, "After calling 'remove' with arg 'forceAll' set to true on the last remaining MidiConnectorMS in widget.wmc.midiConnectors.m[0].value widget.wmc.midiConnectors.m[0].value.size should return 0.");
	}

	test_midiConnect_disconnect {
		var connector1 = widget.wmc.midiConnectors.m[0].value[0];
		var connector2 = widget.addMidiConnector(slot: 0);

		connector1.midiConnect(num: 2, chan: 0, srcID: 12345, argTemplate: 3);
		connector2.midiConnect(num: 3);
		this.assertEquals(widget.wmc.midiConnections.m[0].value[0].class, MIDIFunc, "After connecting a widget's default MidiConnector instance to control nr. 2, channel 0 and source ID 12345 widget.wmc.midiConnections.m.value[0] hold a MIDIFunc");
		this.assertEquals(widget.wmc.midiConnections.m[0].value[0].srcID, 12345, "widget.wmc.midiConnections.m[0].value[0].srcID should return 12345");
		this.assertEquals(widget.wmc.midiConnections.m[0].value[0].chan, 0, "widget.wmc.midiConnections.m[0].value[0].chan should return 0");
		this.assertEquals(widget.wmc.midiConnections.m[0].value[0].msgNum, 2, "widget.wmc.midiConnections.m[0].value[0].msgNum should return 2");
		this.assertEquals(widget.wmc.midiConnections.m[0].value[0].argTemplate, 3, "widget.wmc.midiConnections.m[0].value[0].argTemplate should return 3");
		this.assertEquals(widget.wmc.midiDisplay.m[0].value[0].learn, "X", "widget.wmc.midiDisplay.m[0].value[0].learn should equal \"X\"");
		this.assertEquals(widget.wmc.midiDisplay.m[0].value[0].src, 12345, "widget.wmc.midiDisplay.m[0].value[0].src should equal 12345");
		this.assertEquals(widget.wmc.midiDisplay.m[0].value[0].chan, 0, "widget.wmc.midiDisplay.m[0].value[0].chan should equal 0");
		this.assertEquals(widget.wmc.midiDisplay.m[0].value[0].ctrl, 2, "widget.wmc.midiDisplay.m[0].value[0].ctrl should equal 2");
		this.assertEquals(widget.wmc.midiDisplay.m[0].value[0].template, "3", "widget.wmc.midiDisplay.m[0].value[0].template should equal 3");
		this.assertEquals(widget.wmc.midiDisplay.m[0].value[0].dispatcher.interpret.class, MIDIMessageDispatcher, "widget.wmc.midiDisplay.m[0].value[0].dispatcher.class should equal MIDIMessageDispatcher");
		this.assertEquals(widget.wmc.midiDisplay.m[0].value[0].toolTip, "Click to disconnect", "widget.wmc.midiDisplay.m[0].value[0].template should equal \"Click to disconnect\"");
		connector1.midiDisconnect;
		this.assertEquals(widget.wmc.midiConnections.m[0].value[0], nil, "After disconnecting a widget's default MidiConnector instance widget.wmc.midiConnections.m[0].value[0] should hold nil");
		this.assertEquals(widget.wmc.midiDisplay.m[0].value[0], (ctrl: "ctrl", chan: "chan", src: 'source...', learn: "L", toolTip: "Click and move hardware slider/knob to connect to", slotToolTip: "Select the CVWidgetMS's '%' slot (widget has % slots)."), "After disconnecting a widget's default MidiConnector instance widget.wmc.midiDisplay.m[0].value should hold an Event with the default values: (ctrl: \"ctrl\", chan: \"chan\", src: \"source\", learn: \"L\", toolTip: \"Click and move hardware slider/knob to connect to\", slotToolTip: \"Select the CVWidgetMS's '%' slot (widget has % slots).\")");
		connector1.remove;
		this.assertEquals(widget.wmc.midiConnectors.m[0].value.size, 1, "After removing connector1 widget.wmc.midiConnectors.m.value should hold one MidiConnector.");
		this.assertEquals(widget.wmc.midiConnections.m[0].value[0].class, MIDIFunc, "After calling connection2.midiConnect(num: 3) and calling connection1.remove widget.wmc.midiConnections.m[0].value[0] should hold a MIDIFunc");
	}
}

TestOscConnectorMS : UnitTest {
	var widget;

	setUp {
		widget = CVWidgetMS(\test);
	}

	tearDown {
		widget.remove;
	}

	test_new {
		var connector2, connector3;
		var vals;
		connector2 = OscConnectorMS(widget, slot: 0);
		this.assertEquals(widget.wmc.oscConnectors.m.collect { |slot| slot.value.size }, [2, 1, 1, 1, 1], "After creating another OscConnector for slot 0 the widget hold two OscConnectorMSs in widget.oscConnectors[0]");
		connector3 = OscConnectorMS(widget, "c2", 0);
		this.assertEquals(widget.wmc.oscConnectors.m.collect { |slot| slot.value.size }, [3, 1, 1, 1, 1], "After creating another OscConnector for slot 2 the widget hold three OscConnectors in widget.oscConnectors");
		this.assertEquals(connector3.name, "c2", "The third OscConnector should have been named 'c2'");
		vals = widget.wmc.oscOptions.m[0].value.collect { |v|
			v == (
				oscEndless: false,
				oscResolution: 1,
				oscCalibration: true,
				oscSnapDistance: 0,
				oscInputRange: [0.0001, 0.0001],
				oscInputMapping: (mapping: \linlin),
				oscMatching: false
			);
		};
		this.assertEquals(vals, [true, true, true], "The values of oscOptions model in slot 0 declared within the OscConnectorMS should default to an Event (oscEndless: false, oscResolution: 1, oscCalibration: true, oscSnapDistance: 0.1, oscInputRange: [0.0001, 0.0001], oscInputMapping: (mapping: 'linlin'))");
		vals = widget.wmc.oscDisplay.m[0].value.collect { |v|
			v == (
				nameField: '/path/to/cmd',
				msgSlot: 1,
				connectState: ["learn", Color.yellow, Color.green(0.5)],
				connectEnabled: true, // default, if no command is given
				learn: true, // default, no command given
				numMsgSlots: 1,
				alwaysPositive: 0.1,
				slotToolTip: "Select the CVWidgetMS's '%' slot (widget has % slots)."
			)
		};
		this.assertEquals(vals, [true, true, true], "The values of oscDisplay model declared within the OscConnectors should default to an Event (ipField: nil, portField: nil, nameField: '/path/to/cmd', index: 1, connectorButVal: 0, connect: \"Learn\", slotToolTip: \"Select the CVWidgetMS's '%' slot (widget has % slots).\")");
	}

	test_name {
		this.assertEquals(widget.wmc.oscConnectors.m.collect { |slot| slot.value[0].name }, widget.wmc.oscConnectorNames.m.collect(_.value).collect(_[0]), "On creation a CVWidgetMS should have one OscConnector named 'OSC Connection 1'. This name is held in the widget's midiConnectorNames model value at index 0 in each slot.");
		widget.wmc.midiConnectors.m[0].value[0].name_('xxxx');
		this.assertEquals(widget.wmc.oscConnectors.m.collect { |slot| slot.value[0].name }, widget.wmc.oscConnectorNames.m.collect(_.value).collect(_[0]), "After renaming the MidiConnector the name returned by calling the method 'name' should be equal the widget's midiConnectorNames model value in slot 0 at index 0.");
	}

	test_remove {
		var connector2 = widget.addOscConnector(slot: 0);
		this.assertEquals([
			widget.wmc.oscConnectorNames.m[0].value,
			widget.wmc.oscConnections.m[0].value,
			widget.wmc.oscOptions.m[0].value,
			widget.wmc.oscDisplay.m[0].value
		].collectAs({ |m| m.size }, Set), Set[2], "After adding OscConnectorMS connector2 to widget models in slot 0 related to the widget's connector should hold values of size 2.");
		widget.wmc.oscConnectors.m[0].value[0].remove;
		this.assertEquals(widget.wmc.oscConnectors.m[0].value, List[connector2], "After removing the widget's connector at index 0 in widget.wmc.oscConnectors.m[0].value widget.wmc.oscConnectors.m[0].value should hold a single connector, connector2.");
		this.assertEquals([
			widget.wmc.oscConnectorNames.m[0].value,
			widget.wmc.oscConnections.m[0].value,
			widget.wmc.oscOptions.m[0].value,
			widget.wmc.oscDisplay.m[0].value
		].collectAs({ |m| m.size }, Set), Set[1], "After removing the OscConnector at widget.wmc.oscConnectors.m[0].value[0] widget models related to the widget's connector should hold values of size 1.");
		widget.wmc.oscConnectors.m[0].value[0].remove;
		this.assertEquals(widget.wmc.oscConnectors.m[0].value.size, 1, "After calling 'remove' on the last remaining OscConnector in widget.wmc.oscConnectors.m[0].value widget.wmc.oscConnectors.m[0].value.size should still return 1.");
		widget.wmc.oscConnectors.m[0].value[0].remove(true);
		this.assertEquals(widget.wmc.oscConnectors.m[0].value.size, 0, "After calling 'remove' with arg 'forceAll' set to true on the last remaining OscConnector in widget.wmc.oscConnectors.m[0].value widget.wmc.oscConnectors.m[0].value.size should return 0.");
	}

	test_oscConnect_disconnect {
		var connector1 = widget.oscConnectors[0][0];
		var connector2 = widget.addOscConnector(slot: 0);

		connector1.oscConnect(NetAddr.localAddr, '/test1', 1, argTemplate: 4);
		connector2.oscConnect(NetAddr.localAddr, '/test2', 1, matching: true);
		this.assertEquals(widget.wmc.oscConnections.m[0].value[0].class, OSCFunc, "After connecting a widget's default OscConnector instance widget.wmc.oscConnections.m[0].value[0] should hold an OSCFunc.");
		this.assertEquals(widget.wmc.oscConnections.m[0].value[0].srcID, NetAddr.localAddr, "widget.wmc.oscConnections.m[0].value[0].srcID should return NetAddr.localAddr.");
		this.assertEquals(widget.wmc.oscConnections.m[0].value[0].path, '/test1', "widget.wmc.oscConnections.m[0].value[0].path should return '/test1'.");
		this.assertEquals(widget.wmc.oscConnections.m[0].value[0].recvPort, nil, "widget.wmc.oscConnections.m[0].value[0].recvPort should return nil.");
		this.assertEquals(widget.wmc.oscConnections.m[0].value[0].argTemplate, [0, 1, 2, 3], "widget.wmc.oscConnections.m[0].value[0].argTemplate should return [0, 1, 2, 3].");
		this.assertEquals(widget.wmc.oscDisplay.m[0].value[0].connectState, ["disconnect", Color.white, Color.red], "widget.wmc.oscDisplay.m[0].value[0].connectState should equal [\"disconnect\", Color.white, Color.red].");
		this.assertEquals(widget.wmc.oscDisplay.m[0].value[0].ipField, '127.0.0.1', "widget.wmc.oscDisplay.m[0].value[0].ipField should equal \"127.0.0.1\".");
		this.assertEquals(widget.wmc.oscDisplay.m[0].value[0].portField, 57120, "widget.wmc.oscDisplay.m[0].value[0].portField should equal 57120.");
		this.assertEquals(widget.wmc.oscDisplay.m[0].value[0].nameField, '/test1', "widget.wmc.oscDisplay.m[0].value[0].nameField should equal '/test1'.");
		this.assertEquals(widget.wmc.oscDisplay.m[0].value[0].template, "[0, 1, 2, 3]", "widget.wmc.oscDisplay.m[0].value[0].template should equal [0, 1, 2, 3]");
		this.assertEquals(widget.wmc.oscDisplay.m[0].value[0].dispatcher.class, OSCMessageDispatcher, "widget.wmc.oscDisplay.m[0].value[0].dispatcher.class should return OSCMessageDispatcher");
		connector1.oscDisconnect;
		this.assertEquals(widget.wmc.oscConnections.m[0].value[0], nil, "After disconnecting a widget's default MidiConnector instance widget.wmc.oscConnections.m[0].value[0] should hold nil");
		this.assertEquals(widget.wmc.oscDisplay.m[0].value[0], (
			numMsgSlots: 1,
			msgSlot: 1,
			nameField: '/test1',
			ipField: '127.0.0.1',
			portField: 57120,
			template: "[0, 1, 2, 3]",
			connectState: ["connect", Color.white, Color.blue],
			connectEnabled: true,
			learn: false,
			alwaysPositive: 0.1,
			slotToolTip: "Select the CVWidgetMS's '%' slot (widget has % slots)."
		), "After disconnectiong connector1 widget.wmc.oscDisplay.m[0].value[0] should hold an Event (msgSlot: 1, nameField: '/test1', ipField: \"127.0.0.1\", portField: 57120, template: \"[0, 1, 2, 3]\", connectState: [\"connect\", Color.white, Color.blue])");
		connector1.remove;
		this.assertEquals(widget.wmc.oscConnectors.m[0].value.size, 1, "After removing connector1 widget.wmc.oscConnectors.m[0].value should hold one OscConnector.");
		this.assertEquals(widget.wmc.oscConnections.m[0].value[0].class, OSCFunc, "After calling connection2.oscConnect and calling connection1.remove widget.wmc.oscConnections.m[0].value[0] should hold an OSCFunc");
	}

	// FIXME: This test passes if run as the only test but fails when run calling TestCVCenter.runAll??
	test_osc_input_zero_crossing {
		var n;
		var c = CondVar.new, sigDelay = 0.1;

		fork {
			c.wait({ widget.notNil });
			n = NetAddr.localAddr;
			widget.oscConnect(slot: 0, cmdPath: '/zero_crossing');
			n.sendMsg('/zero_crossing', -0.5);
			sigDelay.wait;
			// c.signalOne;
			// c.wait({ widget.getOscInputAlwaysPositive(0) > 0.1 });
			this.assertFloatEquals(widget.getOscInputAlwaysPositive(0, 0), 0.6, "If input to an OSCFunc is equal or less than equal to 0 input should be normalized to a positive value (adjusting low and high limit).");
		}
	}
}