TestMidiConnector : UnitTest {
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
		connector2 = MidiConnector(widget);
		this.assertEquals(widget.midiConnectors.size, 2, "After creating another MidiConnector the widget hold three MidiConnectors midiConnectors");
		connector3 = MidiConnector(widget, "c2");
		this.assertEquals(widget.midiConnectors.size, 3, "After creating another MidiConnector the widget hold three MidiConnectors midiConnectors");
		this.assertEquals(connector3.name, 'c2', "The third midiConnector should have been named 'c2'");
		vals = widget.wmc.midiOptions.model.collect { |m|
			m.value == (
				midiMode: 0,
				midiMean: 64,
				midiResolution: 1,
				softWithin: 0.1
			);
		};
		this.assertEquals(vals, [true, true, true], "The values of all midiOptions models declared within the MidiConnectors should default to an Event (midiMode: 0, midiMean: 64, midiResolution: 1, softWithin: 0.1)");
		vals = widget.wmc.midiDisplay.model.collect { |m|
			m.value == (
				src: "source",
				chan: "chan",
				ctrl: "ctrl",
				learn: "L"
			)
		};
		this.assertEquals(vals, [true, true, true], "The values of all midiDisplay models declared within the MidiConnectors should default to an Event (src: \"source\", chan: \"chan\", ctrl: \"ctrl\", learn: \"L\")");
	}

	test_midiConnect_disconnect {
		var connector1 = widget.midiConnectors[0];
		var connector2 = widget.addMidiConnector;

		connector1.midiConnect(num: 2, chan: 0, src: 12345);
		connector2.midiConnect(num: 1);
		this.assertEquals(widget.wmc.midiConnections.model[0].value, (num: 2, chan: 0, src: 12345), "After connecting a widget's default MidiConnector instance to control nr. 2, channel 0 and source ID 12345 widget.wmc.midiConnections.model[0].value should return an Event (num: 2, chan: 0, src: 12345)");
		this.assertEquals(widget.wmc.midiDisplay.model[0].value, (learn: "X", src: 12345, chan: 0, ctrl: 2 ), "After connecting a widget's default MidiConnector instance to control nr. 2, channel 0 and source ID 12345 widget.wmc.midiDisplay.model[0].value should return an Event (learn: \"X\", src: 12345, chan: 0, num: 2 )");
		this.assertEquals(widget.wmc.midiConnections.model[1].value, (num: 1), "After connecting connector2 to control nr. 1 widget.wmc.midiConnections.model[1].value should return an Event (num: 1)");
		connector1.midiDisconnect;
		this.assertEquals(widget.wmc.midiConnections.model[0].value, nil, "After disconnecting a widget's default MidiConnector instance widget.wmc.midiConnections.model[0].value should hold nil");
		this.assertEquals(widget.wmc.midiDisplay.model[0].value, (ctrl: "ctrl", chan: "chan", src: "source", learn: "L"), "After disconnecting a widget's default MidiConnector instance widget.wmc.midiDisplay.model[0].value should hold an Event with the default values: (ctrl: \"ctrl\", chan: \"chan\", src: \"source\", learn: \"L\")");
		connector2.midiConnect(num: 3);
		connector1.remove;
		this.assertEquals(widget.wmc.midiConnections.model[0].value, (num: 3), "After calling connection2.midiConnect(num: 2) and calling connection1.remove widget.wmc.midiConnections.model[0].value should hold an Event (num: 3)");
		this.assertEquals(widget.wmc.midiDisplay.model[0].value, (learn: "X", src: "source", chan: "chan", ctrl: 3), "The widget's model at index 0 should hold an Event (learn: \"X\", src: \"source\", chan: \"chan\", ctrl: 3).");
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