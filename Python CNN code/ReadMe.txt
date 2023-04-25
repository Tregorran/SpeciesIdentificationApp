ReadMe, for Models
--------------------
These python files will not work without changing the file locations 
and obtaining the Cub-Birds-200-2011 dataset, in order to use the bird 
images and text files it provides. 
which can be downloaded from:
https://www.vision.caltech.edu/datasets/cub_200_2011/

After this, the bounding box model must first be run before the classification 
model, as it contains the code to move the images from Cub-Birds to the 
all_images folder.

These commented scripts must be run in the order the code appears, in order 
to correctly pre-process the images and bounding boxes before training the 
models.

Specifically, these scripts ran on Google Colab.