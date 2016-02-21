require 'sinatra'
require 'sinatra/json'
require 'sinatra/activerecord'
require 'rack'
require 'rack/contrib'
require 'pp'
require 'models/proof'

use Rack::PostBodyContentTypeParser

post '/sendVote' do
    json :parameters_were => params
end

post '/updateProofsFile' do
    if Proof.count == 0
        Proof.create!( content: params[ "content" ] )
    else
        proof = Proof.first
        proof.update!( content: params[ "content" ] )
    end
end

get '/proofsFile' do
    json :content => Proof.first.content
end
