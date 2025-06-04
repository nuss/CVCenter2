TestMidiConnector : UnitTest {
	var widget;

	setUp {
		CVWidget.initMidiOnStartUp = false;
		widget = CVWidgetKnob(\test);
		CVWidget.midiSources = ('12345': "source 1", '54321': "source 2");
	}

	tearDown {
		widget.remove;
	}

	test_new {
		var connector2, connector3;
		var vals;
		connector2 = MidiConnector(widget);
		this.assertEquals(widget.midiConnectors.size, 2, "After creating another MidiConnector the widget hold three MidiConnectors midiConnectors");
		connector3 = MidiConnector(widget, "c2");
		this.assertEquals(widget.midiConnectors.size, 3, "After creating another MidiConnector the widget hold three MidiConnectors midiConnectors");
		this.assertEquals(connector3.name, "c2", "The third midiConnector should have been named 'c2'");
		vals = widget.wmc.midiOptions.model.value.collect { |v|
			v == (
				midiMode: 0,
				midiZero: 64,
				midiResolution: 1,
				snapDistance: 0.1,
				ctrlButtonGroup: 1
			);
		};
		this.assertEquals(vals, [true, true, true], "The values of midiOptions model declared within the MidiConnectors should default to an Event (midiMode: 0, midiZero: 64, midiResolution: 1, softWithin: 0.1)");
		vals = widget.wmc.midiDisplay.model.value.collect { |v|
			v == (
				src: "source...",
				chan: "chan",
				ctrl: "ctrl",
				learn: "L",
				toolTip: "Click and move hardware slider/knob to connect to"
			)
		};
		this.assertEquals(vals, [true, true, true], "The values of midiDisplay model declared within the MidiConnectors should default to an Event (src: \"source\", chan: \"chan\", ctrl: \"ctrl\", learn: \"L\")");
		vals = widget.wmc.midiInputMappings.model.value.collect { |v|
			v == (mapping: \linlin)
		};
		this.assertEquals(vals, [true, true, true], "The values of midiInputMappings models declared within the MidiConnectors should default to an Event (mapping: \linlin)")
	}

	test_name {
		this.assertEquals(widget.midiConnectors[0].name, widget.wmc.midiConnectorNames.model.value[0], "On creation a CVWidgetKnob should have one MideiConnector named 'MIDI Connection 1'. This name is held in the widget's midiConnectorNames model value at index 0.");
		widget.midiConnectors[0].name_('xxxx');
		this.assertEquals(widget.midiConnectors[0].name, widget.wmc.midiConnectorNames.model.value[0], "After renaming the MidiConnector the name returned by calling the method 'name' should be equal the widget's midiConnectorNames model value at index 0.");
	}

	test_remove {
		var connector2 = widget.addMidiConnector;
		this.assertEquals([
			widget.wmc.midiConnectorNames.model.value,
			widget.wmc.midiConnections.model.value,
			widget.wmc.midiOptions.model.value,
			widget.wmc.midiInputMappings.model.value,
			widget.wmc.midiDisplay.model.value
		].collectAs({ |m| m.size }, Set), Set[2], "After adding MidiConnector connector2 to widget models related to the widget's connector should hold values of size 2.");
		widget.midiConnectors[0].remove;
		this.assertEquals(widget.midiConnectors, List[connector2], "After removing the widget's connector at index 0 in widget.midiConnectors widget.midiConnectors should hold a single connector, connector2, at in widget.midiConnectors.");
		this.assertEquals([
			widget.wmc.midiConnectorNames.model.value,
			widget.wmc.midiConnections.model.value,
			widget.wmc.midiOptions.model.value,
			widget.wmc.midiInputMappings.model.value,
			widget.wmc.midiDisplay.model.value
		].collectAs({ |m| m.size }, Set), Set[1], "After removing the MidiConnector at widget.midiConnectors[0] widget models related to the widget's connector should hold values of size 1.");
		widget.midiConnectors[0].remove;
		this.assertEquals(widget.midiConnectors.size, 1, "After calling 'remove' on the last remaining MidiConnector in widget.midiConnectors widget.midiConnectors.size should still return 1.");
		widget.midiConnectors[0].remove(true);
		this.assertEquals(widget.midiConnectors.size, 0, "After calling 'remove' with arg 'forceAll' set to true on the last remaining MidiConnector in widget.midiConnectors widget.midiConnectors.size should return 0.");
	}

	test_midiConnect_disconnect {
		var connector1 = widget.midiConnectors[0];
		var connector2 = widget.addMidiConnector;

		connector1.midiConnect(num: 2, chan: 0, src: 12345);
		connector2.midiConnect(num: 1);
		this.assertEquals(widget.wmc.midiConnections.model[0].value, (num: 2, chan: 0, src: 12345), "After connecting a widget's default MidiConnector instance to control nr. 2, channel 0 and source ID 12345 widget.wmc.midiConnections.model[0].value should return an Event (num: 2, chan: 0, src: 12345)");
		this.assertEquals(widget.wmc.midiDisplay.model[0].value, (learn: "X", src: 12345, chan: 0, ctrl: 2, toolTip: "Click to disconnect"), "After connecting a widget's default MidiConnector instance to control nr. 2, channel 0 and source ID 12345 widget.wmc.midiDisplay.model[0].value should return an Event (learn: \"X\", src: 12345, chan: 0, num: 2, toolTip: \"Click to disconnect\")");
		this.assertEquals(widget.wmc.midiConnections.model[1].value, (num: 1), "After connecting connector2 to control nr. 1 widget.wmc.midiConnections.model[1].value should return an Event (num: 1)");
		connector1.midiDisconnect;
		this.assertEquals(widget.wmc.midiConnections.model[0].value, nil, "After disconnecting a widget's default MidiConnector instance widget.wmc.midiConnections.model[0].value should hold nil");
		this.assertEquals(widget.wmc.midiDisplay.model[0].value, (ctrl: "ctrl", chan: "chan", src: "source...", learn: "L", toolTip: "Click and move hardware slider/knob to connect to"), "After disconnecting a widget's default MidiConnector instance widget.wmc.midiDisplay.model[0].value should hold an Event with the default values: (ctrl: \"ctrl\", chan: \"chan\", src: \"source\", learn: \"L\", toolTip: \"Click and move hardware slider/knob to connect to\")");
		connector2.midiConnect(num: 3);
		connector1.remove;
		this.assertEquals(widget.midiConnectors.size, 1, "After removing connector1 widget.midiConnectors should hold one MidiConnector.");
		this.assertEquals(widget.wmc.midiConnections.model[0].value, (num: 3), "After calling connection2.midiConnect(num: 2) and calling connection1.remove widget.wmc.midiConnections.model[0].value should hold an Event (num: 3)");
		this.assertEquals(widget.wmc.midiDisplay.model[0].value, (learn: "X", src: "source...", chan: "chan", ctrl: 3, toolTip: "Click to disconnect"), "The widget's model at index 0 should hold an Event (learn: \"X\", src: \"source\", chan: \"chan\", ctrl: 3, toolTip: \"Click to disconnect\").");
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
}