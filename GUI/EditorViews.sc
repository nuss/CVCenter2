OscConnectorsEditorView : CompositeView {
	classvar <all;
	var <widget, <parent, <cIndex;
	// GUI elements
	var <e;

	*initClass {
		all = ();
	}

	*new { |widget, connectorID=0, parent|
		^super.new.init(widget, connectorID, parent.asView);
	}

	init { |wdgt, index, parentView|
		var o;

		// index can be an Integer, a Symbol or a MidiConnector instance
		if (index.class == Symbol) {
			index = widget.wmc.oscConnectors.m.value.detect { |c| c.name == index }
		};
		if (index.class == OscConnector) {
			index = widget.wmc.oscConnectors.m.value.indexOf(index)
		};

		e = ();

		widget = wdgt;
		all[widget] ?? { all[widget] = List[] };
		all[widget].add(this);

		if (parentView.isNil) {
			parent = Window("%: OSC connections".format(widget.name), Rect(0, 0, 300, 300))
		} { parent = parentView };

		if (widget.wmc.oscConnectors.m.value.isEmpty) {
			OscConnector(widget)
		};

		// fallback if index out of bounds
		if (index >= widget.wmc.midiConnectors.m.value.size) {
			index = widget.wmc.midiConnectors.m.value.size - 1;
		};

		e.connectorNameField = OscConnectorNameField(parent, widget, connectorID: index);
		e.connectorSelect = OscConnectorSelect(parent, widget, connectorID: index);
		e.addrSelect = OscAddrSelect(parent, widget, connectorID: index);

		parent.layout_(
			VLayout(
				HLayout(
					[e.connectorNameField, stretch: 9],
					[e.connectorSelect, stretch: 1]
				),
				HLayout(
					[e.addrSelect],
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
			connector = widget.wmc.oscConnectors.m.value[connector]
		};
		cIndex = widget.wmc.oscConnectors.m.value.indexOf(connector);
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

		if (wdgt.wmc.midiConnectors.m.value.isEmpty) {
			MidiConnector(wdgt)
		};

		// index can be an Integer, a Symbol or a MidiConnector instance
		if (index.class == Symbol) {
			index = wdgt.wmc.midiConnectors.m.value.detect { |c| c.name == index }
		};
		if (index.class == MidiConnector) {
			index = wdgt.wmc.midiConnectors.m.value.indexOf(index)
		};
		// after all, if index is nil or greater the set it to 0
		if (index.isNil or: { index > wdgt.wmc.midiConnectors.m.value.lastIndex }) { index = 0 };

		widget = wdgt;
		all[widget] ?? { all[widget] = List[] };
		all[widget].add(this);

		if (parentView.isNil) {
			parent = Window("%: MIDI connections".format(widget.name), Rect(0, 0, 300, 375))
		} { parent = parentView };

		parent.onClose_({ this.close });

		e = ();
		e.connectorNameField = MidiConnectorNameField(parent, widget, connectorID: index);
		e.connectorSelect = MidiConnectorSelect(parent, widget, connectorID: index);
		e.midiModeSelect = MidiModeSelect(parent, widget, connectorID: index);
		e.midiZeroBox = MidiZeroNumberBox(parent, widget, connectorID: index);
		e.snapDistanceBox = SnapDistanceNumberBox(parent, widget, connectorID: index);
		e.midiResolutionBox = MidiResolutionNumberBox(parent, widget, connectorID: index);
		e.slidersPerGroupNB = SlidersPerGroupNumberBox(parent, widget, connectorID: index);
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
					[StaticText(parent).string_("MIDI mode: 0-127 or endless "), stretch: 7],
					[e.midiModeSelect, stretch: 3]
				),
				HLayout(
					[StaticText(parent).string_("MIDI mean (endless mode only): "), stretch: 7],
					[e.midiZeroBox, stretch: 3]
				),
				HLayout(
					[StaticText(parent).string_("snap distance for slider (0-127 only): "), stretch: 7],
					[e.snapDistanceBox, stretch: 3]
				),
				HLayout(
					[StaticText(parent).string_("MIDI resolution (endless mode only): "), stretch: 7],
					[e.midiResolutionBox, stretch: 3]
				),
				HLayout(
					[StaticText(parent).string_("Number of sliders per bank: "), stretch: 7],
					[e.slidersPerGroupNB, stretch: 3]
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
				e.connectorSelect.view.value_(widget.wmc.midiConnectors.m.value.indexOf(m));
			};

			if (sel.value < (sel.items.size - 1)) {
				e.do(_.index_(sel.value));
				// enable or disable selects for MIDI source, channel and ctrl number based on connection status
				[e.midiSrcSelect, e.midiChanTF, e.midiNumTF].do { |elem|
					elem.view.enabled_(widget.wmc.midiConnections.m[sel.value].value.isNil)
				}
			}
		})
	}

	set { |connector|
		if (connector.isInteger) {
			index = connector
		} {
			index = widget.wmc.midiConnectors.m.value.indexOf(connector)
		};
		e.do(_.index_(index));
	}

	widget_ { |otherWidget|
		// FIXME: check for CVWidget2D slot (once it's implemented...)
		if (otherWidget.class !== CVWidgetKnob) {
			Error("Widget must be a CVWidgetKnob").throw
		};

		all[widget].remove(this);
		widget = otherWidget;
		connector = widget.wmc.midiConnectors.m.value[0];
		all[widget] ?? { all[widget] = List[] };
		if (all[widget].includes(this).not) { all[widget].add(this) };
		e.do(_.widget_(widget));
		if (parent.class === Window) {
			parent.name_("%: MIDI connections".format(widget.name))
		}
	}

	front {
		parent.front;
	}

	close {
		all[widget].remove(this);
		e.do(_.close);
	}

	*closeAll {
		all.pairsDo { |key, eds| eds.do(_.close) }
	}
}