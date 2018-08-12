/**
 * Copyright (C) 2013 Aurélien Chabot <aurelien@chabot.fr>
 * 
 * This file is part of DroidUPNP.
 * 
 * DroidUPNP is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * DroidUPNP is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with DroidUPNP.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.droidupnp.view;

import org.droidupnp.Main;
import org.droidupnp.model.upnp.IUpnpDevice;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;

import java.util.Observable;
import java.util.Observer;

public class RendererDeviceFragment extends UpnpDeviceListFragment implements Observer {

	protected static final String TAG = "RendererDeviceFragment";

	public RendererDeviceFragment()
	{
		super();
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);
		Main.upnpServiceController.getRendererDiscovery().addObserver(this);
		Main.upnpServiceController.addSelectedRendererObserver(this);
		Log.d(TAG, "onActivityCreated");
	}

	@Override
	public void onDestroy()
	{
		super.onDestroy();
		Main.upnpServiceController.getRendererDiscovery().removeObserver(this);
		Main.upnpServiceController.delSelectedRendererObserver(this);
		Log.d(TAG, "onDestroy");
	}

	@Override
	protected boolean isSelected(IUpnpDevice device)
	{
		if (Main.upnpServiceController != null && Main.upnpServiceController.getSelectedRenderer() != null)
			return device.equals(Main.upnpServiceController.getSelectedRenderer());

		return false;
	}

	@Override
	protected void select(IUpnpDevice device)
	{
		select(device, false);
	}

	@Override
	protected void select(IUpnpDevice device, boolean force)
	{
		Main.upnpServiceController.setSelectedRenderer(device, force);
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id)
	{
		super.onListItemClick(l, v, position, id);
		select(list.getItem(position).getDevice());
		Log.d(TAG, "Set renderer to " + list.getItem(position));
	}

	@Override
	public void update(Observable observable, Object o)
	{
		Activity a = getActivity();
		if (a == null)
			return;

		a.runOnUiThread(new Runnable() {
			@Override
			public void run()
			{
				IUpnpDevice device = Main.upnpServiceController.getSelectedRenderer();
				if (device == null)
				{
					// Uncheck device
					getListView().clearChoices();
					list.notifyDataSetChanged();
				}
				else
				{
					addedDevice(device);
				}
			}
		});
	}
}