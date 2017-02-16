clc;clear;clf;close all;


filelist = dir('images/*.jpg');
m = 4;
n = 3; %length(filelist)
for i=1:n
    
    %% Load the original image
    imname = filelist(i).name;
	im = imread(['images/' imname], 'jpg');
    

    %% Run FFT on the image
    F = fft2(im2double(im));
    
    height = size(im,1);
    width = size(im,2);
    r1 = height/2 * 0.008;
    r2 = height/2 * 0.3;
    [X,Y] = meshgrid(1:width, 1:height);
    M = sqrt((X-width/2).^2 + (Y-height/2).^2) > r1 & sqrt((X-width/2).^2 + (Y-height/2).^2) < r2;
    
    M = double(M);
    
    C = M.*fftshift(F);

    %% Run IFFT on the image
    I = ifft2(fftshift(C));
    
    % Plot filtered image from IFFT
    figure
    imshow(im2uint8(I));
end