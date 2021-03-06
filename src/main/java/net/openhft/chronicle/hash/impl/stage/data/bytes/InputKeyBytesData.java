/*
 *      Copyright (C) 2012, 2016  higherfrequencytrading.com
 *      Copyright (C) 2016 Roman Leventov
 *
 *      This program is free software: you can redistribute it and/or modify
 *      it under the terms of the GNU Lesser General Public License as published by
 *      the Free Software Foundation, either version 3 of the License.
 *
 *      This program is distributed in the hope that it will be useful,
 *      but WITHOUT ANY WARRANTY; without even the implied warranty of
 *      MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *      GNU Lesser General Public License for more details.
 *
 *      You should have received a copy of the GNU Lesser General Public License
 *      along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.openhft.chronicle.hash.impl.stage.data.bytes;

import net.openhft.chronicle.bytes.*;
import net.openhft.chronicle.hash.AbstractData;
import net.openhft.chronicle.hash.impl.stage.hash.CheckOnEachPublicOperation;
import net.openhft.chronicle.hash.impl.stage.hash.KeyBytesInterop;
import net.openhft.sg.Stage;
import net.openhft.sg.StageRef;
import net.openhft.sg.Staged;

import static net.openhft.chronicle.bytes.NoBytesStore.NO_BYTES_STORE;

@Staged
public class InputKeyBytesData<K> extends AbstractData<K> {

    @StageRef KeyBytesInterop<K> ki;
    @StageRef CheckOnEachPublicOperation checkOnEachPublicOperation;

    @Stage("InputKeyBytesStore") private BytesStore inputKeyBytesStore = null;
    @Stage("InputKeyBytesStore") private long inputKeyBytesOffset;
    @Stage("InputKeyBytesStore") private long inputKeyBytesSize;

    public void initInputKeyBytesStore(BytesStore bytesStore, long offset, long size) {
        inputKeyBytesStore = bytesStore;
        inputKeyBytesOffset = offset;
        inputKeyBytesSize = size;
    }

    @Stage("InputKeyBytes") private final VanillaBytes inputKeyBytes =
            new VanillaBytes(NO_BYTES_STORE);
    @Stage("InputKeyBytes") private boolean inputKeyBytesUsed = false;

    boolean inputKeyBytesInit() {
        return inputKeyBytesUsed;
    }

    void initInputKeyBytes() {
        inputKeyBytes.bytesStore(inputKeyBytesStore, inputKeyBytesOffset, inputKeyBytesSize);
        inputKeyBytesUsed = true;
    }

    void closeInputKeyBytes() {
        inputKeyBytes.bytesStore(NO_BYTES_STORE, 0, 0);
        inputKeyBytesUsed = false;
    }

    @Stage("CachedInputKey") private K cachedInputKey;
    @Stage("CachedInputKey") private boolean cachedInputKeyRead = false;
    
    private void initCachedInputKey() {
        cachedInputKey = innerGetUsing(cachedInputKey);
        cachedInputKeyRead = true;
    }

    @Override
    public RandomDataInput bytes() {
        checkOnEachPublicOperation.checkOnEachPublicOperation();
        return inputKeyBytes.bytesStore();
    }

    @Override
    public long offset() {
        checkOnEachPublicOperation.checkOnEachPublicOperation();
        return inputKeyBytesOffset;
    }

    @Override
    public long size() {
        checkOnEachPublicOperation.checkOnEachPublicOperation();
        return inputKeyBytesSize;
    }

    @Override
    public K get() {
        checkOnEachPublicOperation.checkOnEachPublicOperation();
        return cachedInputKey;
    }

    @Override
    public K getUsing(K using) {
        checkOnEachPublicOperation.checkOnEachPublicOperation();
        return innerGetUsing(using);
    }
    
    private K innerGetUsing(K usingKey) {
        inputKeyBytes.readPosition(inputKeyBytesOffset);
        return ki.keyReader.read(inputKeyBytes, inputKeyBytesSize, usingKey);
    }
}
