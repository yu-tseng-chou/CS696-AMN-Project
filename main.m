% Clean up
clc;clear;clf;close all;

% Loading the images
filelist = dir('images/Strip*.jpg');
n = length(filelist);
numSubplotRows = 4;
numSubplotCols = 1;

for i=1:n
    autoSubplotter = MakeAutoSubplot(numSubplotRows, numSubplotCols);
    
    %% Load the original image
    imname = filelist(i).name;
	im = imread(['images/' imname], 'jpg');
   
    figure
    autoSubplotter();
    imshow(im);
    title(sprintf('Original file %s', imname));
    
    %% Filter image with a median filter and convert it to gray scale
    G = rgb2gray(im);
    G = medfilt2(G);
    
    %% Get the optimal threshold for image filtering
    autoSubplotter();
    [counts,binLocations] = imhist(G);
    stem(binLocations, counts);
    title('Histogram of gray scale version image');
    
    % TO DO: get threshold from between peaks
    
    
    %% Filter image with a threshold so only black and white left
    threshold = ComputeThreshold(counts);
    T = G;
    T(T > threshold) = 255;
    T(T <= threshold) = 0;
    autoSubplotter();
    imshow(T);
    title(sprintf('Filtered image with threshold = %d', threshold));
    
    %% 
    autoSubplotter();
    filterWidth=3;
    decomp = MedianDecomposition(T,filterWidth);
    imshow(repmat(decomp,size(T,1),1));
    title(sprintf('After median decomposition of width = %d', filterWidth));
    
    %% Extract song length information
    height = size(T,1);
    song_lengths = SongLengthsExtraction(decomp);
    g = sprintf('%d, ', song_lengths);
    g = g(1:size(g,2)-2); % Trim off trailing comma
    text(0, height+25, sprintf('%d Songs detected. Lengths: %s', size(song_lengths, 2), g));
    
end