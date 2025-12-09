MappingSelect : CompositeView {
	classvar <all, connectorRemovedFuncAdded;
	var mc, connectors, syncKey, mappingType;
	var <connector, <widget, connectorKind;
	var <e, bgColor;
	var defaultEnv;

	*initClass {
		all = ();
	}

	*new { |parent, widget, rect, connectorID=0, connectorKind, layout([[\mselect, \mcurve, \mplot], [\menv]])|
		if (widget.isKindOf(CVWidget).not) {
			Error("arg 'widget' must be a kind of CVWidget").throw
		};
		^super.new.init(parent, widget, rect, connectorID, connectorKind, layout);
	}

	init { |parentView, wdgt, rect, index, kind, layout|
		var parent, row, i;
		var ramp, env;

		if (kind.isNil) {
			Error("arg 'connectorKind' in MappingSelect.new must not be nil - must either be 'midi' or 'osc'.").throw
		} {
			connectorKind = kind.asSymbol;
			if (connectorKind !== \midi and: { connectorKind !== \osc }) {
				Error("arg 'connectorKind' must be a String or Symbol, either 'midi' or 'osc'. Given: %".format(connectorKind)).throw
			}
		};

		defaultEnv = Env([0, 1], [1]);

		widget = wdgt;
		all[widget] ?? { all[widget] = () };
		all[widget][connectorKind] ?? {
			all[widget][connectorKind] = List[];
		};
		all[widget][connectorKind].add(this);

		case
		{ connectorKind === \midi } {
			mc = widget.wmc.midiOptions;
			connectors = widget.midiConnectors;
			bgColor = Color(0.8, alpha: 0.3);
			mappingType = \midiInputMapping;
		}
		{ connectorKind === \osc } {
			mc = widget.wmc.oscOptions;
			connectors = widget.wmc.oscConnectors.m.value;
			bgColor = Color(green: 0.8, blue: 0.5, alpha: 0.3);
			mappingType = \oscInputMapping;
		};

		// the kind of connector must be known by now, so, index_ should already know

		if (parentView.isNil) {
			parent = Window("%: % mappings".format(widget.name, connectorKind.asString.toUpper), Rect(0, 0, 300, 65))
		} {
			parent = parentView;
		};

		this.background_(bgColor).minHeight_(80);

		ramp = switch (mc.m.value[index][mappingType].mapping)
		{ \linenv } { mc.m.value[index][mappingType].env }
		{ \lincurve } { [\lincurve, mc.m.value[index][mappingType].curve] }
		{ \linbicurve } { [\linbicurve, mc.m.value[index][mappingType].curve] }
		{ mc.m.value[index][mappingType].mapping };

		e = ();
		e.mplot = RampPlot(parent, ramp: ramp).maxHeight_(25);
		e.mselect = PopUpMenu(parent).items_([
			\linlin, \linexp, \explin, \expexp, \lincurve, \linbicurve, \linenv
		]).minHeight_(25);
		e.mcurve = NumberBox(parent).clipHi_(12).clipLo_(-12).minHeight_(25);
		e.menv = TextField(parent).minHeight_(25)
		.string_((mc.m.value[index][mappingType].env ? Env([0, 1], [1])).asCompileString);

		case
		{ mc.m.value[index][mappingType].mapping === \lincurve or: {
			mc.m.value[index][mappingType].mapping === \linbicurve
		}} {
			e.mcurve.enabled_(true);
			e.menv.enabled_(false);
		}
		{ mc.m.value[index][mappingType].mapping === \linenv } {
			e.menv.enabled_(true);
			e.mcurve.enabled_(false);
		}
		{
			e.menv.enabled_(false);
			e.mcurve.enabled_(false);
		};

		if (layout.size > 1) {
			this.layout_(VLayout());
			layout.size.do { |i|
				row = HLayout();
				layout[i].do { |k| row.add(e[k]) };
				this.layout.add(row)
			};
		} {
			row = HLayout();
			layout[0].do { |k| row.add(e[k]) };
			this.layout.add(row)
		};

		this.index_(index);
		this.onClose_({ this.close });

		e.mselect.action_({ |sel|
			i = connectors.indexOf(this.connector);
			env = if (e.menv.string.interpret.class == Env) {
				e.menv.string.interpret
			} { defaultEnv };

			case
			{ sel.value == 4 or: { sel.value == 5 }} {
				mc.m.value[i][mappingType].mapping = sel.items[sel.value];
				mc.m.value[i][mappingType].curve = e.mcurve.value;
				mc.m.value[i][mappingType].env = nil;
			}
			{ sel.value == 6 } {
				mc.m.value[i][mappingType].env = env;
				mc.m.value[i][mappingType].mapping = sel.items[sel.value];
				mc.m.value[i][mappingType].curve = nil;
			}
			{
				mc.m.value[i][mappingType].mapping = sel.items[sel.value];
				mc.m.value[i][mappingType].env = nil;
				mc.m.value[i][mappingType].curve = nil;
			};
			mc.m.changedPerformKeys(widget.syncKeys, i);
		});
		e.mcurve.action_({ |nb|
			i = connectors.indexOf(this.connector);
			if (e.mselect.value == 4 or: { e.mselect.value == 5 }) {
				mc.m.value[i][mappingType].curve = nb.value;
				mc.m.changedPerformKeys(widget.syncKeys, i)
			}
		});
		e.menv.action_({ |tf|
			i = connectors.indexOf(this.connector);
			env = if (tf.string.interpret.class == Env) {
				tf.string.interpret
			} { defaultEnv };

			if (e.mselect.value == 6) {
				mc.m.value[i][mappingType].env = env;
				mc.m.changedPerformKeys(widget.syncKeys, i)
			}
		});
		connectorRemovedFuncAdded ?? {
			MidiConnector.onConnectorRemove_({ |widget, id|
				this.prOnRemoveConnector(widget, id, connectorKind)
			});
			connectorRemovedFuncAdded = true
		};
		this.prAddController;
	}

	index_ { |connectorID|
		// "connectorID: %".format(connectorID).postln;
		connector = connectors[connectorID];
		mc.m.value[connectorID] !? {
			e.mselect.value_(e.mselect.items.indexOf(mc.m.value[connectorID][mappingType].mapping));
			e.mcurve.value_(mc.m.value[connectorID][mappingType].curve ? 0);
			e.menv.string_((mc.m.value[connectorID][mappingType].env ? defaultEnv).asCompileString);
			case
			{ mc.m.value[connectorID][mappingType].mapping === \lincurve or: {
				mc.m.value[connectorID][mappingType].mapping === \linbicurve
			}} {
				e.mplot.draw([mc.m.value[connectorID][mappingType].mapping, mc.m.value[connectorID][mappingType].curve]);
				e.menv.enabled_(false);
				e.mcurve.enabled_(true);
			}
			{ mc.m.value[connectorID][mappingType].mapping === \linenv } {
				e.mplot.draw(mc.m.value[connectorID][mappingType].env);
				e.menv.enabled_(true);
				e.mcurve.enabled_(false);
			}
			{
				e.mplot.draw(mc.m.value[connectorID][mappingType].mapping);
				e.menv.enabled_(false);
				e.mcurve.enabled_(false);
			}
		}
	}

	widget_ { |otherWidget|
		var ramp;

		// FIXME: check for CVWidget2D slot (once it's implemented...)
		if (otherWidget.class !== CVWidgetKnob) {
			Error("Widget must be a CVWidgetKnob").throw
		};

		all[otherWidget] ?? { all[otherWidget] = () };
		all[otherWidget][connectorKind] ?? {
			all[otherWidget][connectorKind] = List[];
		};
		all[otherWidget][connectorKind].add(this);

		this.prCleanup;
		// switch after cleanup has finished
		widget = otherWidget;

		case
		{ connectorKind === \midi } {
			mc = widget.wmc.midiOptions;
			connectors = widget.midiConnectors;
			mappingType = \midiInputMapping;
		}
		{ connectorKind === \osc } {
			mc = widget.wmc.oscOptions;
			connectors = widget.wmc.oscConnectors.m.value;
			mappingType = \oscInputMapping;
		};

		ramp = switch (mc.m.value[0][mappingType].mapping)
		{ \linenv } { mc.m.value[0][mappingType].env }
		{ \lincurve } { [\lincurve, mc.m.value[0][mappingType].curve] }
		{ \linbicurve } { [\linbicurve, mc.m.value[0][mappingType].curve] }
		{ mc.m.value[0][mappingType].mapping };

		case
		{ mc.m.value[0][mappingType].mapping === \lincurve or: {
			mc.m.value[0][mappingType].mapping === \linbicurve
		}} {
			e.mcurve.enabled_(true);
			e.menv.enabled_(false);
		}
		{ mc.m.value[0][mappingType].mapping === \linenv } {
			e.menv.enabled_(true);
			e.mcurve.enabled_(false);
		}
		{
			e.menv.enabled_(false);
			e.mcurve.enabled_(false);
		};

		// midiConnector at index 0 should always exist (who knows...)
		this.index_(0);
		this.prAddController;
	}

	prAddController {
		var conID;
		mc.c ?? { mc.c = SimpleController(mc.m) };
		syncKey = (connectorKind ++ this.class.asString).asSymbol;
		widget.syncKeys.indexOf(syncKey) ?? {
			widget.prAddSyncKey(syncKey, true)
		};
		mc.c.put(syncKey, { |changer, what ... moreArgs|
			conID = moreArgs[0];
			all[widget][connectorKind].do { |ms, i|
				if (ms.connector === connectors[conID]) {
					{
						ms.e.mselect.value_(e.mselect.items.indexOf(changer.value[conID][mappingType].mapping));
						case
						{ changer.value[conID][mappingType].mapping === \lincurve or: {
							changer.value[conID][mappingType].mapping === \linbicurve
						}} {
							ms.e.mcurve.value_(changer.value[conID][mappingType].curve).enabled_(true);
							ms.e.mplot.draw([changer.value[conID][mappingType].mapping, changer.value[conID][mappingType].curve]);
							ms.e.menv.enabled_(false);
						}
						{ changer.value[conID][mappingType].mapping === \linenv } {
							ms.e.mcurve.enabled_(false);
							ms.e.mplot.draw(changer.value[conID][mappingType].env ? defaultEnv);
							ms.e.menv.string_(changer.value[conID][mappingType].env.asCompileString).enabled_(true);
						}
						{
							ms.e.mcurve.enabled_(false);
							ms.e.mplot.draw(changer.value[conID][mappingType].mapping);
							ms.e.menv.enabled_(false);
						}
					}.defer
				}
			}
		})
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

	close {
		this.remove;
		e.do(_.close);
		this.prCleanup;
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
}

RampPlot : SCViewHolder {
	var <>background, <>foreground;

	*new { |parent, rect, ramp = \linlin, background(Color.blue(0.3)), foreground(Color.cyan)|
		^super.newCopyArgs(nil, background, foreground).init(parent, rect, ramp)
	}

	init { |parentView, rect, ramp|
		this.view = UserView(parentView, rect)
		.background_(this.background)
		.minWidth_(40)
		.minHeight_(25);
		this.draw(ramp);
		this.onClose_({ this.close });
	}

	draw { |ramp|
		var rampVals = this.prCreateRampVals(ramp);

		this.view.background_(this.background);
		this.view.drawFunc_({ |v|
			Pen.strokeColor_(this.foreground).width_(2).moveTo(0@this.view.bounds.height);
			(rampVals.size - 1).do { |i|
				Pen.lineTo(Point(
					this.view.bounds.width/rampVals.size*(i+1),
					this.view.bounds.height-(rampVals[i+1] * this.view.bounds.height)
				))
			};
			Pen.stroke;
		});
		this.view.refresh
	}

	prCreateRampVals { |ramp|
		// rampArray models a linear ramp;
		var rampArray = (0, 0.01..1);

		switch (ramp.class)
		{ Array } {
			if (ramp.size < 2) {
				"if arg 'ramp' is given as an Array it must contain two values: the first one denoting the mapping method (\\lincurve or \\linbicurve) and the second one a number denoting the curve caracteristics".error;
				^rampArray;
			} {
				ramp[0] = ramp[0].asSymbol;
				if (ramp[1].isNumber) {
					switch (ramp[0])
					{ \lincurve } {
						^rampArray.perform(ramp[0], 0, 1, 0, 1, ramp[1])
					}
					{ \linbicurve } {
						^rampArray.perform(ramp[0], 0, 1, nil, 0, 1, nil, ramp[1])
					}
				}
			}
		}
		{ Env } {
			if (ramp.respondsTo(\asMultichannelSignal).not) {
				"No valid envelope given - defaulting to Env([0, 1], [1], \lin)".error;
				^rampArray
			} {
				^rampArray.linenv(0, 1, 0, 1, ramp, \minmax, rampArray.size)
			}
		}
		{ ^rampArray.perform(ramp.asSymbol, 0.02, 1, 0.02, 1) }
	}

}