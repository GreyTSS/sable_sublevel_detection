
# Sable: Sublevel Detection - Dead Man's Switch
This mod, as well as its assets, were created by human hands. No Generative AI was used for any of the code, English, or art in this mod.

A mod that currently adds two sensors to interact with Sable sublevels and give players redstone feedback related to whether a sublevel is being tracked by a player or not.

Through exposed methods usable in-game through CC: Tweaked's CraftOS, you can gain access to the names or UUIDs of those occupying your sublevel, as well as the occupancy count of the entire sublevel.

As you can hopefully see, this opens the door to a huge range of divergent behaviors. The most obvious is as the subtitle of the mod states, dead man's switches. Just hook up an occupancy sensor in inverted state to a clutch providing power to your Aeronautics vessel, and it will automatically stop in the unfortunate case of your fall overboard.

The imaginative can take this behavior further through the assistance of CC: Tweaked, and employ occupancy and sublevels as ways to query physical space in the real world in a brand new way. In-Game Cafes can be programmed to supply the host with a list of new occupants waiting to be tended to. Create: Simulated rollercoasters can be made to wait patiently for a rider, or a set amount of riders, before disembarking automatically. The applications are vast.


## Dependencies

 - Required
    - [Sable 2.0+ (Tested 2.0.5)](https://github.com/ryanhcode/sable)
 - Optional
    - [CC: Tweaked (Tested 1.115.1)](https://github.com/cc-tweaked/cc-tweaked)

- Recommended
    - [Create Aeronautics](https://github.com/Creators-of-Aeronautics/Simulated-Project)
## Features

- Occupancy Sensor: 
    * A block that emits a Redstone Signal when on a Sable Sublevel that is currently occupied by one or many players. 
    * Signal can be inverted by right-clicking with an empty hand.
    
- Seated Occupancy Sensor:
    * A block that emits a Redstone Signal when on a Sable Sublevel that is currently occupied by one or many *seated* players.
    * Signal can be inverted by right-clicking with an empty hand. 

- Sensor Swapping:
    * Sensors each are crafted with a similar crafting recipe, aside from one unique item. The Occupancy sensor has an Eye of Ender, while a Seated Occupancy Sensor uses a Minecart.
    * A placed sensor can be swapped to a different sensor type by right-clicking it while holding the desired sensor's unique crafting item.
    * The previous sensor's unique item is ejected during the swap.


- Optional CC:Tweaked Compatibility. Both the Occupancy Sensor, and Seated Occupancy Sensor, can be wrapped as a peripheral, exposing the following functions inside of CraftOS:
    * getOccupancyCount()
    * getOccupantsNames()
    * getOccupantsUUIDs()
    * getInversionState()
    * setInversionState(true/false)



## Support

This is my first ever attempt at modding, and as such, my inexperience may manifest in some unforeseen bugs. 

Please feel free to add a ticket, explaining your bug and how it can be replicated, as well as any relevant logs, screenshots or videos, and I will do my best to solve it as quickly as possible.


## Optimizations

Although this is my first mod, I did try to optimize it to the best of my ability. 

- Sensors register themselves to be used in polling, unregistering on load
- Only sensors that exist on sublevels are capable of registering themselves
- The entirety of the mods logic is bypassed if no sensors exist within the registry
- Players are polled periodically, according to the config, to determine which sublevels are being occupied serverwide. The collections used are pooled.
- Any state-changing operations are only used if the occupancy lists have changed since the previous polling.
- Only specific sublevels that have changed occupancy states are targeted by block updates.

This is the best I could figure to optimize it within the bounds of what Sable provides as open api, as the author has stated events declaring tracking changes are unfeasible. Profiling has shown this mods tick event to take an average of 35k nanoseconds with hundreds of sensors in a single player instance, and seemed to scale well in multiplayer testing.




## Roadmap
v1.2
- Add a decay system to the Occupancy Sensor, allowing for each block to be configured to drop their redstone signal linearly across a defined tick amount the moment a sublevel becomes unoccupied. 

- Ponder sequence for the sensor.

- Animation and particles for sensor texture.

Future:
- More sensor types


## Credits:
Tefra_K: Artist
