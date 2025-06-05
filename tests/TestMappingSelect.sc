TestMappingSelect : UnitTest {
	var widget, ms1, ms2;

	setUp {
		widget = CVWidgetKnob(\test);
		ms1 = MappingSelect(widget: widget, connectorKind: \midi);
	}

	tearDown {
		ms1.close;
		ms2.close;
		widget.remove;
	}

	test_new {
		this.assert(MappingSelect.all[widget][\midi][0] === ms1, "MappingSelect.all[widget] should hold a list with a single MappingSelect instance.");
		this.assertEquals(widget.syncKeys, [\default, (\midi ++ MappingSelect.asString).asSymbol], "The widget's 'syncKeys' should contain  two Symbols, 'default' and 'midiMappingSelect', after creating a new MappingSelect");
		this.assert(ms1.connector === widget.midiConnectors[0], "The elements connector should be identical with the connector at the widget's midiConnectors List at index 0");
		widget.setMidiInputMapping(\lincurve, 3);
		this.assertEquals(ms1.e.mselect.value, 4, "After calling widget.setMidiInputMapping('lincurve', 3) the MappingSelect ms1's PopUpMenu should have been set to the item at index 4");
		this.assertEquals(ms1.e.mcurve.value, 3, "After calling widget.setMidiInputMapping('lincurve', 3) the MappingSelect ms1's NumberBox should have been set to a value 3");
		ms2 = MappingSelect(widget: widget, connectorKind: \midi);
		this.assertEquals(ms2.e.mselect.value, 4, "After creating the MappingSelect ms2 its PopUpMenu should have been set to the item at index 4");
		this.assertEquals(ms2.e.mcurve.value, 3, "After creating the MappingSelect ms2 its NumberBox should have been set to a value 3");
		widget.setMidiInputMapping(\linenv, env: Env([0, 0.2, 1], [0.7, 0.5], [-3, 4]));
		this.assertEquals(ms1.e.menv.string, Env([0, 0.2, 1], [0.7, 0.5], [-3, 4]).asCompileString, "After calling widget.setMidiInputMapping('linenv', Env([0, 0.2, 1], [0.7, 0.5], [-3, 4])) the MappingSelect ms1's envelope TextField should have been set to the string 'Env([0, 0.2, 1], [0.7, 0.5], [-3, 4])'");
		this.assertEquals(ms2.e.menv.string, Env([0, 0.2, 1], [0.7, 0.5], [-3, 4]).asCompileString, "After calling widget.setMidiInputMapping('linenv', Env([0, 0.2, 1], [0.7, 0.5], [-3, 4])) the MappingSelect ms1's envelope TextField should have been set to the string 'Env([0, 0.2, 1], [0.7, 0.5], [-3, 4])'");
	}

	test_index_ {
		widget.addMidiConnector;
		ms1.index_(1);
		this.assert(ms1.connector === widget.midiConnectors[1], "After adding a MidiConnector to the widget and calling ms1.index_(1) ms1's 'connector' variable should be identical with the widget's MidiConnector at index 1");
		widget.addMidiConnector;
		ms2 = MappingSelect(widget: widget, connectorID: 2, connectorKind: \midi);
		this.assert(ms2.connector === widget.midiConnectors[2], "After adding another MidiConnector to the widget and creating MappingSelect ms2 with arg 'connectorID' set to 2' ms2's 'connector' variable should be identical with the widget's MidiConnector at indes 2.")
	}

	test_close {
		ms2 = MappingSelect(widget: widget, connectorKind: \midi);
		ms1.close;
		this.assertEquals(widget.syncKeys, [\default, (\midi ++ MappingSelect.asString).asSymbol], "After closing MappingSelect ms1 widget.syncKeys should hold 2 Symbols: 'default' and 'MappingSelect'.");
		ms2.close;
		this.assertEquals(widget.syncKeys, [\default], "After closing MappingSelect ms2 widget.syncKeys should hold 1 Symbol: 'default'.");
	}

}