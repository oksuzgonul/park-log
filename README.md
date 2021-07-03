# ParkLog App for Android

This is a simple app to track the location of a parked car, I made this one for the Udacity Android Developer Nanodegree as a Capstone project.

It uses ViewModel and LiveData to display the data saved for the parked cars (like the location pin, pictures, car details) that is saved in an Android Room database.

It uses BroadcastReceiver to display the notifications before the park timer expires.

It uses a basic RecyclerView to list the cars on the MainActivity:

![image](https://user-images.githubusercontent.com/47000155/124347622-7e93e400-db9a-11eb-897f-c5e6bb36de69.png)

Tapping on the + button takes user to the NewParkActivity where Google Maps API is utilized to use the location of the user:

![image](https://user-images.githubusercontent.com/47000155/124347641-8c496980-db9a-11eb-9441-4c1251a009e5.png)

Whereas tapping on one of the listed cars takes user to the car details:

![image](https://user-images.githubusercontent.com/47000155/124347642-8eabc380-db9a-11eb-8309-b6002ee60d30.png)

Within the car details if user taps on the parking history, where user can see the pictures taken, car and parking spot details:

![image](https://user-images.githubusercontent.com/47000155/124347645-910e1d80-db9a-11eb-86d1-009a98279371.png)

Tapping on GET CAR NOW button takes user to the Google Maps App, with the saved location pin.
