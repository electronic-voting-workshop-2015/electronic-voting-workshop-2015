require 'models/publication'
require 'pry'

post '/publish' do
    publication = Publication.create!( content: params[ "content" ].to_json )
end

get '/retrieve' do
    all = Publication.all.to_a.map do |publication| 
        hash = JSON.parse publication.content
        hash
    end
    all.to_json
end
