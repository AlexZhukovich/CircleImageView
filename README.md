# CircleImageView
CircleImageView is a component which display circle image with customization options

<img src="https://github.com/AlexZhukovich/CircleImageView/blob/master/screenshots/animated_circleImageView.gif" width="255px" height="475px" />
<img src="https://github.com/AlexZhukovich/CircleImageView/blob/master/screenshots/non_animated_circleImageView.gif" width="272px" height="475px" />

Release notes
--------
##### 1.2.0
* Implemented animation for pressed event
* Added new attribute pressedRingWidth, pressedRingColor

##### 1.1.4
* Fix bug (issue #3 on github)

##### 1.1.3
* Improve component performance
* Optimization of image drawing

##### 1.1.2
* Support padding parameters (paddingLeft, paddingTop, paddingRight, paddingBottom).

##### 1.1.1
* Fix bug (issue #1 on github).

##### 1.1.0
* shows shadow for image;
* shows shadow for border.

##### 1.0.0
* change background for *.png images;
* shows images;
* realize click listener for this component;
* shows border (component is selected or component is unselected).

Usage
--------
To make a circular ImageView, add this CircleImageView library to your project and add CircleImageView in your layout XML. 
You can also grab it via Gradle:

```groovy
      compile 'com.alexzh:circleimageview:1.2.0'
```

or Maven:

```xml
<dependency>
    <groupId>com.alexzh</groupId>
    <artifactId>circleimageview</artifactId>
    <version>1.2.0</version>
    <type>pom</type>
</dependency>
```

### XML
```xml
    <com.alexzh.circleimageview.CircleImageView
        android:id="@+id/imageView"
        android:layout_width="192dp"
        android:layout_height="192dp"
        android:clickable="true"
        android:src="@drawable/android_logo"
        android:layout_alignParentTop="true"
        android:layout_centerHorizontal="true"
        android:layout_marginTop="10dp"
        app:view_backgroundColor="@color/colorPrimary"
        app:view_shadowRadius="4dp"
        app:view_shadowDx="2dp"
        app:view_shadowDy="0dp"
        app:view_shadowColor="@color/grey"
        app:view_borderWidth="4dp"
        app:view_selectedColor="@color/blue"
        app:view_borderColor="@android:color/darker_gray"/>
```

You may use the following properties in your XML to customize your CircularImageView.

##### Properties:

* `app:view_backgroundColor`    (color)       
* `app:view_borderColor`        (color)
* `app:view_borderWidth`        (dimension)  
* `app:view_selectedColor`      (color)    
* `app:view_shadowRadius`       (dimension)
* `app:view_shadowDx`           (dimension)
* `app:view_shadowDy`           (dimension)
* `app:view_shadowColor`        (color)

### JAVA

* `setOnItemSelectedClickListener(ItemSelectedListener listener)` - let's handle onSelected(View view) and onUnselected(View view)

* `onSelected(View view)` - view is selected
* `onUnselected(View view)` - view is unselected


Developed By
--------

Aliaksandr Zhukovich - http://alexzh.com

License
--------
```
Copyright (C) 2016 Aliaksandr Zhukovich (http://alexzh.com)

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0	     

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
