/////////////////////////////////////////////////////////////////////////////
// This file is part of EasyRPG Player.
//
// EasyRPG Player is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// EasyRPG Player is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with EasyRPG Player. If not, see <http://www.gnu.org/licenses/>.
/////////////////////////////////////////////////////////////////////////////

////////////////////////////////////////////////////////////
// Headers
////////////////////////////////////////////////////////////
#include "game_actors.h"
#include "main_data.h"

////////////////////////////////////////////////////////////
/// Constructor
////////////////////////////////////////////////////////////
Game_Actors::Game_Actors()
{
	// Actors start with index 1
	data.resize(Main_Data::data_actors.size()+1);
}

////////////////////////////////////////////////////////////
/// Destructor
////////////////////////////////////////////////////////////
Game_Actors::~Game_Actors() {
	unsigned int i;
	for (i = 0; i < data.size(); i++) {
		delete data[i];
	}
}

////////////////////////////////////////////////////////////
/// Subscript []-operator
////////////////////////////////////////////////////////////
Game_Actor* Game_Actors::GetActor(int actorId) {
	// Invalid Index (LDB has less actors)
	if (actorId <= 0 || (unsigned)actorId >= data.size() || actorId > 5000) {
		return NULL;
	} else if (data[actorId] == 0) {
		data[actorId] = new Game_Actor(actorId);
	}

	return data[actorId];
}