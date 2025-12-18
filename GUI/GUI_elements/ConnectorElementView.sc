ConnectorElementView : SCViewHolder {
	var mc, conModel, syncKey;
	var toolTip;

	close {
		this.remove;
		this.viewDidClose;
		this.prCleanup;
	}

	prCleanup {
		this.class.all[this.widget].remove(this);
		if (this.class.all[this.widget].isEmpty) {
			mc.c.removeAt(syncKey);
			this.widget.prRemoveSyncKey(syncKey, true);
		}
	}

	// suitable for all instances, hence widget must
	// be passed in explicitly within MidiConnector:-remove
	prOnRemoveConnector { |widget, index|
		if (index > 0) {
			this.class.all[widget].do(_.index_(index - 1))
		} {
			this.class.all[widget].do(_.index_(index))
		}
	}
}

ConnectorNameField : ConnectorElementView {
	classvar <all, connectorRemovedFuncAdded;
	var <connector, <widget, connectorKind, cclass;

	*initClass {
		all = ();
	}

	*new { |parent, widget, rect, connectorID=0, connectorKind|
		if (widget.isKindOf(CVWidget).not) {
			Error("arg 'widget' must be a kind of CVWidget").throw
		};
		^super.new.init(parent, widget, rect, connectorID, connectorKind);
	}

	init { |parentView, wdgt, rect, index, kind|
		if (kind.isNil) {
			Error("arg 'connectorKind' in MappingSelect.new must not be nil - must either be 'midi' or 'osc'.").throw
		} {
			connectorKind = kind.asSymbol;
			if (connectorKind !== \midi and: { connectorKind !== \osc }) {
				Error("arg 'connectorKind' must be a String or Symbol, either 'midi' or 'osc'. Given: %".format(connectorKind)).throw
			}
		};

		widget = wdgt;
		all[widget] ?? { all[widget] = () };
		all[widget][connectorKind] ?? {
			all[widget][connectorKind] = List[]
		};
		all[widget][connectorKind].add(this);

		case
		{ connectorKind === \midi } {
			mc = widget.wmc.midiConnectorNames;
			conModel = widget.midiConnectors;
		}
		{ connectorKind === \osc } {
			mc = widget.wmc.oscConnectorNames;
			conModel = widget.oscConnectors;
		};

		this.view = TextField(parentView, rect);
		this.index_(index);
		this.view.action_({ |tf|
			this.connector.name_(tf.string.asSymbol)
		});
		this.view.onClose_({ this.close });
		// FIXME: don't let funcs pile up in onRemove
		connectorRemovedFuncAdded ?? {
			case
			{ connectorKind === \midi } { cclass = MidiConnector }
			{ connectorKind === \osc } { cclass = OscConnector };
			cclass.onConnectorRemove_({ |widget, id|
				this.prOnRemoveConnector(widget, id, connectorKind)
			});
			connectorRemovedFuncAdded = true
		};
		this.prAddController;
	}

	index_ { |connectorID|
		connector = conModel[connectorID];
		mc.m.value !? {
			this.view.string_(mc.m.value[connectorID])
		}
	}

	widget_ { |otherWidget|
		// FIXME: check for CVWidget2D slot (once it's implemented...)
		if (otherWidget.class !== CVWidgetKnob) {
			Error("Widget must be a CVWidgetKnob").throw
		};

		all[otherWidget] ?? { all[otherWidget] = () };
		all[otherWidget][connectorKind] ?? {
			all[otherWidget][connectorKind] = List[]
		};
		all[otherWidget][connectorKind].add(this);

		this.prCleanup;
		// switch after cleanup has finished
		widget = otherWidget;
		case
		{ connectorKind === \midi } {
			mc = widget.wmc.midiConnectorNames;
			conModel = widget.midiConnectors;
		}
		{ connectorKind === \osc } {
			mc = widget.wmc.oscConnectorNames;
			conModel = widget.oscConnectors;
		};
		// midiConnector at index 0 should always exist (who knows...)
		this.index_(0);
		this.prAddController;
	}

	prAddController {
		var conID;
		mc.c ?? {
			mc.c = SimpleController(mc.m)
		};
		syncKey = (connectorKind ++ this.class.asString).asSymbol;
		widget.syncKeys.indexOf(syncKey) ?? {
			widget.prAddSyncKey(syncKey, true)
		};
		// if there's already a function defined for synKey simply replace it
		mc.c.put(syncKey, { |changer, what ... moreArgs|
			conID = moreArgs[0];
			all[widget][connectorKind].do { |tf|
				if (tf.connector === conModel[conID]) {
					tf.view.string_(changer.value[conID]);
				}
			}
		})
	}

	prCleanup {
		all[widget][connectorKind].remove(this);
		try {
			if (all[widget][connectorKind].notNil and: { all[widget][connectorKind].isEmpty }) {
				mc.c.removeAt(syncKey);
				widget.prRemoveSyncKey(syncKey, true);
				all[widget].removeAt(connectorKind);
			}
		}
	}

	prOnRemoveConnector { |widget, index, connectorKind|
		// if widget has already been removed let it fail
		try {
			if (index > 0) {
				all[widget][connectorKind].do(_.index_(index - 1))
			} {
				all[widget][connectorKind].do(_.index_(index))
			}
		}
	}

}

ConnectorSelect : ConnectorElementView {
	classvar <all, connectorRemovedFuncAdded;
	var <connector, <widget, cons, connectorKind, cclass;
	var selItem0;

	*initClass {
		all = ();
	}

	*new { |parent, widget, rect, connectorID=0, connectorKind|
		if (widget.isKindOf(CVWidget).not) {
			Error("arg 'widget' must be a kind of CVWidget").throw
		};
		^super.new.init(parent, widget, rect, connectorID, connectorKind);
	}

	init { |parentView, wdgt, rect, index, kind|
		if (kind.isNil) {
			Error("arg 'connectorKind' in MappingSelect.new must not be nil - must either be 'midi' or 'osc'.").throw
		} {
			connectorKind = kind.asSymbol;
			if (connectorKind !== \midi and: { connectorKind !== \osc }) {
				Error("arg 'connectorKind' must be a String or Symbol, either 'midi' or 'osc'. Given: %".format(connectorKind)).throw
			}
		};

		widget = wdgt;
		all[widget] ?? { all[widget] = () };
		all[widget][connectorKind] ?? {
			all[widget][connectorKind] = List[]
		};
		all[widget][connectorKind].add(this);

		case
		{ connectorKind === \midi } {
			mc = widget.wmc.midiConnectorNames;
			cons = widget.wmc.midiConnectors;
			selItem0 = 'add MidiConnector...'
		}
		{ connectorKind === \osc } {
			mc = widget.wmc.oscConnectorNames;
			cons = widget.wmc.oscConnectors;
			selItem0 = 'add OscConnector...'
		};

		this.view = PopUpMenu(parentView)
		.items_(mc.m.value ++ [selItem0]);
		this.view.onClose_({ this.close });
		this.index_(index);
		connectorRemovedFuncAdded ?? {
			case
			{ connectorKind === \midi } { cclass = MidiConnector }
			{ connectorKind === \osc } { cclass = OscConnector };
			cclass.onConnectorRemove_({ |widget, id|
				this.prOnRemoveConnector(widget, id, connectorKind)
			});
			connectorRemovedFuncAdded = true
		};
		this.prAddController;
	}

	index_ { |connectorID|
		connector = cons.m.value[connectorID];
		this.view.value_(connectorID);
	}

	widget_ { |otherWidget|
		// FIXME: check for CVWidget2D slot (once it's implemented...)
		if (otherWidget.class !== CVWidgetKnob) {
			Error("Widget must be a CVWidgetKnob").throw
		};

		all[otherWidget] ?? { all[otherWidget] = () };
		all[otherWidget][connectorKind] ?? {
			all[otherWidget][connectorKind] = List[]
		};

		all[otherWidget][connectorKind].add(this);
		this.prCleanup;
		// switch after cleanup has finished
		widget = otherWidget;
		case
		{ connectorKind === \midi } {
			mc = widget.wmc.midiConnectorNames;
			cons = widget.wmc.midiConnectors;
		}
		{ connectorKind === \osc } {
			mc = widget.wmc.oscConnectorNames;
			cons = widget.wmc.oscConnectors;
		};
		this.view.items_(mc.m.value ++ this.view.items.last);
		// midiConnector at index 0 should always exist (who knows...)
		this.index_(0);
		this.prAddController;
	}

	prAddController {
		var items, conID;
		var curValue;
		mc.c ?? {
			mc.c = SimpleController(mc.m)
		};
		cons.c ?? {
			cons.c = SimpleController(cons.m)
		};
		syncKey = (connectorKind ++ this.class.asString).asSymbol;
		widget.syncKeys.indexOf(syncKey) ?? {
			widget.prAddSyncKey(syncKey, true)
		};
		cons.c.put(syncKey, { |changer, what ... moreArgs|
			all[widget][connectorKind].do { |sel, i|
				curValue = sel.view.value;
				sel.view.items_(mc.m.value ++ sel.view.items.last)
				.value_(curValue);
			}
		});
		mc.c.put(syncKey, { |changer, what ... moreArgs|
			conID = moreArgs[0];
			all[widget][connectorKind].do { |sel, i|
				items = sel.view.items;
				items[conID] = changer.value[conID];
				curValue = sel.view.value;
				sel.view.items_(items).value_(curValue);
				if (sel.connector === cons.m.value[conID]) {
					sel.view.value_(conID)
				}
			}
		})
	}

	prCleanup {
		all[widget][connectorKind].remove(this);
		try {
			if (all[widget][connectorKind].notNil and: { all[widget][connectorKind].isEmpty }) {
				mc.c.removeAt(syncKey);
				cons.c.removeAt(syncKey);
				widget.prRemoveSyncKey(syncKey, true);
				all[widget].removeAt(connectorKind);
			}
		}
	}

	prOnRemoveConnector { |widget, index, connectorKind|
		// if widget has already been removed let it fail
		try {
			if (index > 0) {
				all[widget][connectorKind].do(_.index_(index - 1))
			} {
				all[widget][connectorKind].do(_.index_(index))
			}
		}
	}
}

ConnectorRemoveButton : ConnectorElementView {
	classvar <all, connectorRemovedFuncAdded;
	var <connector, <widget, connectorKind, cclass;

	*initClass {
		all = ();
	}

	*new { |parent, widget, rect, connectorID=0, connectorKind|
		if (widget.isKindOf(CVWidget).not) {
			Error("arg 'widget' must be a kind of CVWidget").throw
		};
		^super.new.init(parent, widget, rect, connectorID, connectorKind);
	}

	init { |parentView, wdgt, rect, index, kind|
		if (kind.isNil) {
			Error("arg 'connectorKind' in MappingSelect.new must not be nil - must either be 'midi' or 'osc'.").throw
		} {
			connectorKind = kind.asSymbol;
			if (connectorKind !== \midi and: { connectorKind !== \osc }) {
				Error("arg 'connectorKind' must be a String or Symbol, either 'midi' or 'osc'. Given: %".format(connectorKind)).throw
			}
		};

		widget = wdgt;
		all[widget] ?? { all[widget] = () };
		all[widget][connectorKind] ?? {
			all[widget][connectorKind] = List[]
		};
		all[widget][connectorKind].add(this);

		case
		{ connectorKind === \midi } {
			conModel = widget.midiConnectors;
		}
		{ connectorKind === \osc } {
			conModel = widget.oscConnectors;
		};

		this.index_(index);
		this.view = Button(parentView, rect)
		.states_([["remove Connector", Color.white, Color(0, 0.5, 0.5)]])
		.action_({ this.connector.remove });
		connectorRemovedFuncAdded ?? {
			case
			{ connectorKind === \midi } { cclass = MidiConnector }
			{ connectorKind === \osc } { cclass = OscConnector };
			cclass.onConnectorRemove_({ |widget, id|
				this.prOnRemoveConnector(widget, id, connectorKind)
			});
			connectorRemovedFuncAdded = true
		}
	}

	index_ { |connectorID|
		connector = conModel[connectorID];
	}

	widget_ { |otherWidget|
		// FIXME: check for CVWidget2D slot (once it's implemented...)
		if (otherWidget.class !== CVWidgetKnob) {
			Error("Widget must be a CVWidgetKnob").throw
		};

		all[otherWidget] ?? { all[otherWidget] = () };
		all[otherWidget][connectorKind] ?? {
			all[otherWidget][connectorKind] = List[]
		};
		all[otherWidget][connectorKind].add(this);

		this.prCleanup;
		// switch after cleanup has finished
		widget = otherWidget;
		this.index_(0);
	}

	close {
		this.remove;
		this.viewDidClose;
		this.prCleanup;
	}

	prCleanup {
		all[widget][connectorKind].remove(this);
		try {
			if (all[widget][connectorKind].notNil and: { all[widget][connectorKind].isEmpty }) {
				all[widget].removeAt(connectorKind);
			}
		}
	}

	prOnRemoveConnector { |widget, index, connectorKind|
		// if widget has already been removed let it fail
		try {
			if (index > 0) {
				all[widget][connectorKind].do(_.index_(index - 1))
			} {
				all[widget][connectorKind].do(_.index_(index))
			}
		}
	}
}

// displays current ControlSpec,
// independent from MIDI and OSC,
// resp., current connector
ControlSpecText : ConnectorElementView {
	classvar <all;
	var <widget;

	*initClass {
		all = ();
	}

	*new { |parent, widget, rect|
		if (widget.isKindOf(CVWidget).not) {
			Error("arg 'widget' must be a kind of CVWidget").throw
		};
		^super.new.init(parent, widget, rect)
	}

	init { |parentView, wdgt, rect|
		widget = wdgt;
		all[widget] ?? { all[widget] = List[] };
		all[widget].add(this);

		mc = widget.wmc.cvSpec;

		this.view = StaticText(parentView, rect)
		.string_("Current ControlSpec:\n%".format(mc.m.value));
		this.view.onClose_({ this.close });
		this.prAddController;
	}

	index_ {}

	widget_ { |otherWidget|
		// FIXME: check for CVWidget2D slot (once it's implemented...)
		if (otherWidget.class !== CVWidgetKnob) {
			Error("Widget must be a CVWidgetKnob").throw
		};

		all[otherWidget] ?? { all[otherWidget] = List[] };
		all[otherWidget].add(this);
		this.prCleanup;
		mc = widget.wmc.cvSpec;
		this.prAddController;
	}

	prAddController {
		mc.c ?? {
			mc.c = SimpleController(mc.m)
		};
		syncKey = this.class.asSymbol;
		widget.syncKeys.indexOf(syncKey) ?? {
			widget.prAddSyncKey(syncKey, true)
		};
		mc.c.put(syncKey, { |changer, what ... moreArgs|
			all[widget].do { |txt|
				defer { txt.string_("Current ControlSpec:\n%".format(changer.value)) }
			}
		})
	}
}
