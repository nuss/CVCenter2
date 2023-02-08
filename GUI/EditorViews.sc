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
					e.connectionSelect = PopUpMenu(parent),
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
				e.connectionButton = Button(parent)
			)
		);
	}

	set { |connection|
		if (connection.class == Symbol) {
			connection = widget.oscConnectors.detect { |c| c.name == connection }
		};
		if (connection.isInteger) {
			connection = widget.oscConnectors[connection]
		};
		cIndex = widget.oscConnectors.indexOf(connection);
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
		// index can be an Integer, a Symbol or a MidiConnector instance
		if (index.class == Symbol) {
			index = widget.midiConnectors.detect { |c| c.name == index }
		};
		if (index.class == MidiConnector) {
			index = widget.midiConnectors.indexOf(index)
		};

		e ?? { e = () };
		widget = wdgt;
		if (all[widget].isNil) {
			all[widget] = List[]
		};
		all[widget].add(this);

		if (parentView.isNil) {
			parent = Window("%: MIDI connections".format(widget.name), Rect(0, 0, 300, 300))
		} { parent = parentView };

		parent.onClose_({
			this.close
		});

		if (widget.midiConnectors.isEmpty) {
			MidiConnector(widget)
		};

		e.connectionSelect = MidiConnectorSelect(parent, widget, connectorID: index);
		e.midiModeSelect = MidiModeSelect(parent, widget, connectorID: index);
		e.midiMeanBox = MidiMeanNumberBox(parent, widget, connectorID: index);
		e.softWithinBox = SoftWithinNumberBox(parent, widget, connectorID: index);
		e.midiResolutionBox = MidiResolutionNumberBox(parent, widget, connectorID: index);
		e.slidersPerBankTF = SlidersPerBankNumberTF(parent, widget, connectorID: index);
		e.midiLearnButton = MidiLearnButton(parent, widget, connectorID: index);
		e.midiSrcSelect = MidiSrcSelect(parent, widget, index);
		e.midiChanTF = MidiChanField(parent, widget, index);
		e.midiNumTF = MidiCtrlField(parent, widget, index);

		parent.layout_(
			VLayout(
				HLayout(e.connectionSelect),
				HLayout(
					StaticText(parent).string_("MIDI mode: 0-127 or in/decremental "),
					e.midiModeSelect
				),
				HLayout(
					StaticText(parent).string_("MIDI mean (in/decremental mode only): "),
					e.midiMeanBox
				),
				HLayout(
					StaticText(parent).string_("min. snap distance for slider (0-127 only): "),
					e.softWithinBox
				),
				HLayout(
					StaticText(parent).string_("MIDI resolution (+/- only): "),
					e.midiResolutionBox
				),
				HLayout(
					StaticText(parent).string_("Number of sliders per bank: "),
					e.slidersPerBankTF
				),
				HLayout(
					StaticText(parent).string_("Yaddayadda")
				),
				HLayout(
					e.midiLearnButton,
					e.midiSrcSelect,
					e.midiChanTF,
					e.midiNumTF
				)
			)
		);

		e.connectionSelect.view.action_({ |sel|
			e.do { |el|
				if (sel.value.asBoolean) {
					el.index_(sel.value - 1)
				}
			}
		});
	}

	set { |connector|
		if (connector.class == Symbol) {
			connector = widget.midiConnectors.detect { |c| c.name == connector }
		};
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
}