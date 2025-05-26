OscConnectorsEditorView : CompositeView {
	var <widget, <mc, <parent, <cIndex;
	// GUI elements
	var <e;

	// FIXME: an OscConnector is a single connection
	// yet, a selection of connections can only belong
	// to the widget
	// -> must be considered when adding the model to the widget's
	// model - maybe the model can't be kept with the view?
	*new { |widget, connectorID=0, parent|
		^super.new.init(widget, connectorID, parent.asView);
	}

	init { |wdgt, index, parentView|
		// index can be an Integer, a Symbol or a MidiConnector instance
		if (index.class == Symbol) {
			index = widget.oscConnectors.detect { |c| c.name == index }
		};
		if (index.class == OscConnector) {
			index = widget.oscConnectors.indexOf(index)
		};

		e ?? { e = () };
		widget = wdgt;
		if (parentView.isNil) {
			parent = Window("%: OSC connections".format(widget.name), Rect(0, 0, 300, 300))
		} { parent = parentView };
		// Just create a new OscConnector if none exists
		// it will automatically be added to the widget's list
		// of OscConnectors within OscConnector:-init - should
		// also update the connectionSelect's items
		if (widget.oscConnectors.isEmpty) {
			OscConnector(widget)
		};
		mc = widget.wmc;
		parent.layout_(
			VLayout(
				HLayout(
					e.connectorSelect = PopUpMenu(parent),
					e.addButton = Button(parent).states_([["+"]]),
					e.removeButton = Button(parent).states_([["-"]])
				),
				HLayout(
					e.ipSelect = PopUpMenu(parent),
					StaticText(parent).string_("restrict to port"),
					e.restrictToPortCheckBox = CheckBox(parent)
				),
				StaticText(parent).string_("OSC command name - either select from list provided by the selected device or set custom one"),
				HLayout(
					e.deviceSelect = PopUpMenu(parent),
					e.oscCmdSelect = PopUpMenu(parent),
					e.newDeviceBut = Button(parent)
				),
				HLayout(
					e.oscCmdTextField = TextField(parent),
					e.oscCmdSlotNumBox = NumberBox(parent)
				),
				StaticText(parent).string_("OSC input constraints, zero-crossing correction"),
				HLayout(
					e.inputConstraintsLoNumBox = NumberBox(parent),
					e.inputConstraintsHiNumBox = NumberBox(parent),
					e.zeroCrossCorrectStaticText = StaticText(parent),
					e.calibrationButton = Button(parent),
					e.resetButton = Button(parent),
				),
				e.specConstraintsStaticText = StaticText(parent).string_("current widget spec constraints (lo/hi): 0/0"),
				StaticText(parent).string_("input to output mapping"),
				e.inOutMappingSelect = PopUpMenu(parent),
				e.connectorButton = Button(parent)
			)
		);
	}

	set { |connector|
		if (connector.isInteger) {
			connector = widget.oscConnectors[connector]
		};
		cIndex = widget.oscConnectors.indexOf(connector);
		e.do(_.index_(cIndex));
	}

	front {
		parent.front;
	}
}

MidiConnectorsEditorView : CompositeView {
	classvar <all;
	var <widget, <parent, <index;
	var <connector;
	// GUI elements
	var <e;

	*initClass {
		all = ();
	}

	*new { |widget, connector=0, parent|
		^super.new.init(widget, connector, parent.asView);
	}

	init { |wdgt, index, parentView|
		var m;
		// index can be an Integer, a Symbol or a MidiConnector instance
		if (index.class == Symbol) {
			index = widget.midiConnectors.detect { |c| c.name == index }
		};
		if (index.class == MidiConnector) {
			index = widget.midiConnectors.indexOf(index)
		};

		e = ();
		widget = wdgt;
		all[widget] ?? { all[widget] = List[] };
		all[widget].add(this);

		if (parentView.isNil) {
			parent = Window("%: MIDI connections".format(widget.name), Rect(0, 0, 300, 375))
		} { parent = parentView };

		parent.onClose_({
			this.close
		});

		if (widget.midiConnectors.isEmpty) {
			MidiConnector(widget)
		};

		// fallback if index out of bounds
		if (index >= widget.midiConnectors.size) {
			index = widget.midiConnectors.size - 1;
		};

		e.connectorNameField = MidiConnectorNameField(parent, widget, connectorID: index);
		e.connectorSelect = MidiConnectorSelect(parent, widget, connectorID: index);
		e.midiModeSelect = MidiModeSelect(parent, widget, connectorID: index);
		e.midiZeroBox = MidiZeroNumberBox(parent, widget, connectorID: index);
		e.snapDistanceBox = SnapDistanceNumberBox(parent, widget, connectorID: index);
		e.midiResolutionBox = MidiResolutionNumberBox(parent, widget, connectorID: index);
		e.slidersPerGroupTF = SlidersPerGroupNumberTF(parent, widget, connectorID: index);
		e.midiLearnButton = MidiLearnButton(parent, widget, connectorID: index);
		e.midiSrcSelect = MidiSrcSelect(parent, widget, connectorID: index);
		e.midiChanTF = MidiChanField(parent, widget, connectorID: index);
		e.midiNumTF = MidiCtrlField(parent, widget, connectorID: index);
		e.mappingSelect = MappingSelect(parent, widget, connectorID: index, connectorKind: \midi);
		e.midiInit = MidiInitButton(parent);
		e.midiConnectorRemove = MidiConnectorRemoveButton(parent, widget, connectorID: index);

		parent.layout_(
			VLayout(
				HLayout(
					[e.connectorNameField, stretch: 9],
					[e.connectorSelect, stretch: 1]
				),
				HLayout(
					[StaticText(parent).string_("MIDI mode: 0-127 or in/decremental "), stretch: 7],
					[e.midiModeSelect, stretch: 3]
				),
				HLayout(
					[StaticText(parent).string_("MIDI mean (in/decremental mode only): "), stretch: 7],
					[e.midiZeroBox, stretch: 3]
				),
				HLayout(
					[StaticText(parent).string_("snap distance for slider (0-127 only): "), stretch: 7],
					[e.snapDistanceBox, stretch: 3]
				),
				HLayout(
					[StaticText(parent).string_("MIDI resolution (+/- only): "), stretch: 7],
					[e.midiResolutionBox, stretch: 3]
				),
				HLayout(
					[StaticText(parent).string_("Number of sliders per bank: "), stretch: 7],
					[e.slidersPerGroupTF, stretch: 3]
				),
				HLayout(
					[e.mappingSelect]
				),
				HLayout(
					[e.midiLearnButton, stretch: 1],
					[e.midiSrcSelect, stretch: 4],
					[e.midiChanTF, stretch: 4],
					[e.midiNumTF, stretch: 4]
				),
				HLayout(
					[e.midiInit],
					[e.midiConnectorRemove]
				)
			)
		);

		e.connectorSelect.view.action_({ |sel|
			if (sel.value == (sel.items.size - 1)) {
				m = widget.addMidiConnector;
				e.connectorSelect.view.value_(widget.midiConnectors.indexOf(m));
			};

			if (sel.value < (sel.items.size - 1)) {
				e.do(_.index_(sel.value));
				// enable or disable selects for MIDI source, channel and ctrl number based on connection status
				[e.midiSrcSelect, e.midiChanTF, e.midiNumTF].do { |elem|
					elem.view.enabled_(widget.wmc.midiConnections.model[sel.value].value.isNil)
				}
			}
		})
	}

	set { |connector|
		if (connector.isInteger) {
			index = connector
		} {
			index = widget.midiConnectors.indexOf(connector)
		};
		e.do(_.index_(index));
	}

	front {
		parent.front;
	}

	close {
		all[widget].remove(this);
		e.do(_.close);
	}

	*closeAll {
		all.do { |eds| eds.do(_.close) }
	}
}