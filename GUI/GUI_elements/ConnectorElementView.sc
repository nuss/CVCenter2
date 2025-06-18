ConnectorElementView : SCViewHolder {
	var mc, syncKey;
	var toolTip;

	close {
		this.remove;
		this.viewDidClose;
		this.prCleanup;
	}

	prCleanup {
		this.class.all[this.widget].remove(this);
		if (this.class.all[this.widget].isEmpty) {
			mc.controller.removeAt(syncKey);
			this.widget.prRemoveSyncKey(syncKey, true);
		}
	}

	prOnRemoveConnector { |widget, index, connectorKind|
		var connectors;

		// "%: prOnRemoveConnector called".format(this.class).postln;
		switch (connectorKind)
		{ \midi } { connectors = widget.midiConnectors }
		{ \osc } { connectors = widget.oscConnectors };
		// index = connectors.indexOf(this.connector);

		// [connectors, index, widget, this.class, this.class.all[widget]].postln;
		if (index > 0) {
			this.class.all[widget].do(_.index_(index - 1))
		} {
			this.class.all[widget].do(_.index_(index))
		}
	}
}
