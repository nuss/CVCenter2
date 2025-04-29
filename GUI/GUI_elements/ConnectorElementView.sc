ConnectorElementView : SCViewHolder {
	var mc, syncKey;
	var toolTip;

	close {
		this.remove;
		this.viewDidClose;
		this.class.all[this.widget].remove(this);
		if (this.class.all[this.widget].isEmpty) {
			mc.controller.removeAt(syncKey);
			this.widget.prRemoveSyncKey(syncKey, true);
		}
	}
}
