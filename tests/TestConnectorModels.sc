TestMidiConnector : UnitTest {
	var widget;

	setUp {
		widget = CVWidgetKnob(\test);
	}

	tearDown {
		widget.remove;
	}

	test_new {
		var connector1, connector2;

		connector1 = MidiConnector(widget);
		this.assertEquals(widget.midiConnectors.size, 2, "After creating a new MidiConnector the widget's midiConnectors variable should hold two MidiConnector");
		connector2 = MidiConnector(widget, "c2");
		this.assertEquals(widget.midiConnectors.size, 3, "After creating another MidiConnector the widget's midiConnectors variable should hold three MidiConnectors");
		this.assertEquals(connector2.name, 'c2', "The third midiConnector should have been named 'c2'");
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