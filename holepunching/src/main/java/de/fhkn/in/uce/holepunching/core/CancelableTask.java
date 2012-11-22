/**
 * Copyright (C) 2011 Daniel Maier
 * 
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */

package de.fhkn.in.uce.holepunching.core;

/**
 * The {@link CancelableTask} interface should be implemented by tasks that can
 * be canceled via the {@link CancelableTask#cancel()} method.
 * 
 * @author Daniel Maier
 * 
 */
public interface CancelableTask extends Runnable {
    /**
     * Cancels the current tasks.
     */
    public void cancel();
}
