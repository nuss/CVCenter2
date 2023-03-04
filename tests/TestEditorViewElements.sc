TestMidiConnectorElementView : UnitTest {
	var widget, element1, element2;

	setUp {
		widget = CVWidgetKnob(\test);
		element1 = MidiConnectorNameField(widget: widget);
	}

	tearDown {
		element1.close;
		element2.close;
		widget.remove;
	}

	test_close {
		element2 = MidiConnectorNameField(widget: widget);
		element1.close;
		this.assertEquals(MidiConnectorNameField.all[widget], List[element2], "After closing element1 the class' 'all' variable should hold a List with one element which is element2 under a key which is the widget itself.");
		this.assertEquals(widget.syncKeys, [\default, \midiConnectorName], "The widget's 'syncKeys' should contain  two Symbols, 'default' and 'midiConnectorName', after calling 'close'");
		element2.close;
		this.assertEquals(MidiConnectorNameField.all[widget], List[], "After closing element2 the class' 'all' variable should hold an empty List under a key which is the widget itself.");
		this.assertEquals(widget.syncKeys, [\default], "The widget's 'syncKeys' should contain a single Symbol, 'default' after calling 'close'");
	}
}

TestMidiConnectorNameField : UnitTest {
	var widget, element1, element2;

	setUp {
		widget = CVWidgetKnob(\test);
		element1 = MidiConnectorNameField(widget: widget);
	}

	tearDown {
		element1.close;
		element2.close;
		widget.remove;
	}

	test_new {
		this.assert(MidiConnectorNameField.all[widget][0] === element1, "MidiConnectorNameField's all variable at the key which is the widget itself should hold a List with one value: the element itself.");
		this.assert(element1.connector === widget.midiConnectors[0], "The elements connector should be identical with the connector at the widget's midiConnectors List");
		this.assertEquals(widget.wmc.midiConnectorNames.model.value, List['MIDI Connection 1'], "The widget's midiConnectorNames model should hold a List with one value 'MIDI Connection 1'");
		element1.view.valueAction_('new name');
		this.assertEquals(widget.wmc.midiConnectorNames.model.value, List['new name'], "After calling 'valueAction' on the element's view the widget's midiConnectorNames model should hold a List with one value 'new name'");
		this.assertEquals(widget.midiConnectors[0].name, 'new name', "widget.midiConnectors[0].name should return 'new name' after  calling element1.view.valueAction_('new name')");
		element2 = MidiConnectorNameField(widget: widget);
		this.assertEquals(MidiConnectorNameField.all[widget].size, 2, "MidiConnectorNameField.all[widget] should hold a List with 2 elements after creating another MidiConnectorNameField instance.");
		this.assertEquals(element2.view.string, "new name", "element2's textfield should hold a string 'new name'");
		element2.view.valueAction_('changed name');
		this.assertEquals(element1.view.string, "changed name", "After calling valueAction on element2's view with a value 'changed name' element1's view should have been updated accordingly");
	}

	test_index_ {
		element2 = MidiConnectorNameField(widget: widget);
		widget.addMidiConnector;
		this.assertEquals(element2.connector, widget.midiConnectors[0], "On instantiation a new MidiConnectorNameField's connector should be widget.midiConnectors[0].");
		element2.index_(1);
		this.assertEquals(element2.connector, widget.midiConnectors[1], "After executing element2.index_(1) element2's connector should be MidiConnector widget.midiConnectors[1]");
		this.assertEquals(element2.view.string, "MIDI Connection 2", "After executing element2.index_(1) element2's view should hold a string 'MIDI Connection 2'");
		element1.index_(1);
		element2.valueAction_("another name");
		this.assertEquals(element1.view.string, "another name", "After calling element2.view.valueAction_(\"another name\") element1's view string should have been set to \"another name\"");
	}
}