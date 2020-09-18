Bug details:

The Main activity is horizontally split. The top section exhibits the bug, the bottom section does not. 
The only difference between the top and bottom section is that the top section is wrapped in a viewpager2, and the bottom section is not. 

The top section does not invoke the recycler view items click handlers when tapped, unless the recycler view has been scrolled down a bit. 

The click handlers are invoked if the SwipeRefreshLayout layout_height is set to match_parent, but i need the SwipeRefreshLayout to match constraints. 
